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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.noarg.gradle.KotlinJpaSubplugin
import org.slf4j.LoggerFactory

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
            kotlinVersion.convention(KOTLIN_VERSION) // TODO read from classpath
        }

        extensions.getByType(AllOpenExtension::class.java).apply {
            annotation("javax.persistence.Entity")
            annotation("javax.persistence.MappedSuperclass")
        }

        val kotlin = extensions.getByType(KotlinProjectExtension::class.java)
        kotlin.sourceSets.maybeCreate("main").dependencies {
            // we need to do that lazily here in order to evaluate the KotlinConfigurePluginExtension
            // not too early to fetch the configured kotlin version
            this.implementation(project.provider {
                // see https://kotlinlang.org/docs/gradle.html#dependency-on-the-standard-library
                // as we allow clients to override the Kotlin Version, (i.e. to 1.5.20), we also want to ensure
                // that in this case the kotlin-stdtlib-jdk8 from exactly that Kotlin version is being added
                // to the dependencies. Without those lines, we would always add the stdlib in the version
                // of the underlying Kotlin Gradle Plugin (1.6.20 at the time of that writing)
                val kotlinConfigureExtension = extensions.getByType(KotlinConfigurePluginExtension::class)
                val kotlinVersion = kotlinConfigureExtension.kotlinVersion.get()
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
            })
        }

        // https://kotlinlang.org/docs/gradle.html#gradle-java-toolchains-support
        val javaConfigureExtension = extensions.getByType(JavaConfigurePluginExtension::class)
        kotlin.jvmToolchain {
            (it as JavaToolchainSpec).languageVersion.set(javaConfigureExtension.languageVersion)
        }

        project.afterEvaluate {
            val kotlinConfigureExtension = extensions.getByType(KotlinConfigurePluginExtension::class)
            val kotlinVersion = kotlinConfigureExtension.kotlinVersion.get()
            val kotlinMajorMinor = kotlinVersion.toMajorMinor()

            tasks.withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions.apiVersion = kotlinMajorMinor
                it.kotlinOptions.languageVersion = kotlinMajorMinor
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KotlinConfigurePlugin::class.java)

        const val EXTENSION_NAME = "kotlinConfigure"
    }

    private fun String.toMajorMinor(): String {
        return this.substringBeforeLast(".")
    }
}