package io.cloudflight.gradle.autoconfigure.kotlin

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.allopen.gradle.SpringGradleSubplugin
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.KotlinJpaSubplugin

class KotlinConfigurePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(JavaConfigurePlugin::class)

        project.plugins.apply(KotlinPluginWrapper::class)

        project.plugins.apply(Kapt3GradleSubplugin::class)
        project.plugins.apply(KotlinJpaSubplugin::class) // TODO only if JPA entities detected on classpath
        project.plugins.apply(SpringGradleSubplugin::class)   // TODO only when there is Spring on the classpath

        val extensions = project.extensions
        val tasks = project.tasks

        extensions.create(EXTENSION_NAME, KotlinConfigurePluginExtension::class).apply {
            kotlinVersion.convention(project.getKotlinPluginVersion())
        }

        extensions.getByType(AllOpenExtension::class.java).apply {
            annotation("javax.persistence.Entity")
            annotation("javax.persistence.MappedSuperclass")
        }

        val kotlinConfigureExtension = extensions.getByType(KotlinConfigurePluginExtension::class)

        val kotlin = extensions.getByType(KotlinProjectExtension::class)
        kotlin.sourceSets.maybeCreate("main").dependencies {
            // see https://kotlinlang.org/docs/gradle.html#dependency-on-the-standard-library
            // as we allow clients to override the Kotlin Version, (i.e. to 1.5.20), we also want to ensure
            // that all kotlin standard libraries from exactly that Kotlin version is being added
            // to the dependencies. That's why we add the kotlin-bom in exactly our version here.
            // Without those lines, we would always add the stdlib in the version
            // of the underlying Kotlin Gradle Plugin (1.7.20 at the time of that writing)
            this.implementation(kotlinConfigureExtension.kotlinVersion
                .map { project.dependencies.platform("org.jetbrains.kotlin:kotlin-bom:$it") }
            )
            this.implementation(kotlinConfigureExtension.kotlinVersion
                .map { "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$it" }
            )
        }

        val javaConfigurePluginExtension = extensions.getByType(JavaConfigurePluginExtension::class)
        kotlin.jvmToolchain {
            // see https://youtrack.jetbrains.com/issue/KT-51104/Docs-Build-Tools-Gradle-Setting-toolchain-via-Java-extension-doe
            // `kotlinOptions.jvmTarget` does not correctly get configured when only the `java { toolchain { languageVersion = ... } }` is applied
            // we need to explicitly configure it in the `kotlin.jvmToolchain` as well.
            // But we cannot use the `java.toolchain` for it as it should be the same toolchain object instance for java and kotlin
            // and therefor would lead to a stackoverflow during property resolution.
            // see: https://youtrack.jetbrains.com/issue/KT-43095/Add-support-for-Java-Toolchain-to-the-Gradle-plugin#focus=Comments-27-5173612.0-0
            (it as JavaToolchainSpec).languageVersion.set(javaConfigurePluginExtension.languageVersion)
        }

        project.afterEvaluate {
            val kotlinVersion = kotlinConfigureExtension.kotlinVersion.get()
            val kotlinMajorMinor = kotlinVersion.toMajorMinor()

            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions.apiVersion = kotlinMajorMinor
                it.kotlinOptions.languageVersion = kotlinMajorMinor
                it.kotlinOptions.freeCompilerArgs =
                    listOf("-Xjsr305=strict") // https://kotlinlang.org/docs/java-interop.html#jsr-305-support
            }
        }
    }

    companion object {
        const val EXTENSION_NAME = "kotlinConfigure"
    }

    private fun String.toMajorMinor(): String {
        return this.substringBeforeLast(".")
    }
}