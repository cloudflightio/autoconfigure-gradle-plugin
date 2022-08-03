package io.cloudflight.gradle.autoconfigure.java

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.gorylenko.GitPropertiesPlugin
import io.cloudflight.gradle.autoconfigure.application.development.DevelopmentExtension
import io.cloudflight.gradle.autoconfigure.application.shadow.ShadowExtension
import io.cloudflight.gradle.autoconfigure.application.springboot.SpringBootExtension
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.project.withPlugin
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.tasks.named
import io.cloudflight.gradle.autoconfigure.git.GitExtension
import io.cloudflight.gradle.autoconfigure.util.BuildUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.springframework.boot.gradle.plugin.SpringBootPlugin



class JavaConfigurePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(JavaLibraryPlugin::class)
        project.plugins.apply(JacocoPlugin::class)

        val extensions = project.extensions
        val tasks = project.tasks

        val javaConfigureExtension = extensions.create(EXTENSION_NAME, JavaConfigurePluginExtension::class).apply {
            languageVersion.convention(JAVA_LANGUAGE_VERSION)
            encoding.convention(JAVA_ENCODING)
            vendorName.convention(VENDOR_NAME)
            applicationBuild.convention(false)
            createSourceArtifacts.convention(applicationBuild.map { !it })
            applicationFramework.convention(ApplicationFramework.SpringBoot)
        }

        val javaPluginExtension = extensions.getByType(JavaPluginExtension::class)
        javaPluginExtension.modularity.inferModulePath.set(true)
        javaPluginExtension.toolchain.languageVersion.set(javaConfigureExtension.languageVersion)

        tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class).configure {
            it.doFirst(PopulateManifestAction)
        }

        tasks.named(JavaPlugin.TEST_TASK_NAME, Test::class).configure {
            it.useJUnitPlatform()
        }

        project.afterEvaluate {

            if (javaConfigureExtension.createSourceArtifacts.get()) {
                javaPluginExtension.withSourcesJar()
                javaPluginExtension.withJavadocJar()
            }

            if (javaConfigureExtension.applicationBuild.get()){
                when (javaConfigureExtension.applicationFramework.get()) {
                    ApplicationFramework.SpringBoot -> {
                        if (BuildUtils.isIntegrationBuild()) {
                            GitExtension.create(project)
                            SpringBootExtension.create(project)
                        } else {
                            DevelopmentExtension.create(project)
                        }
                    }
                    ApplicationFramework.Micronaut -> {
                        ShadowExtension.create(project)
                    }
                    else -> {}
                }
            }

            val compileJava = tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile::class)
            compileJava.configure {
                it.options.encoding = javaConfigureExtension.encoding.get()
            }

            val compileTest = tasks.named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, JavaCompile::class)
            compileTest.configure {
                it.options.encoding = javaConfigureExtension.encoding.get()
            }
        }

        // TODO pull that out of the JavaConfigurePlugin?
        project.withPlugin(SpringBootPlugin::class.java) {
            SpringBootExtension.configure(project)
        }
        project.withPlugin(ShadowPlugin::class.java) {
            ShadowExtension.configure(project)
        }
        project.withPlugin(GitPropertiesPlugin::class.java) {
            GitExtension.configure(project)
        }
    }

    companion object {
        const val EXTENSION_NAME = "javaConfigure"
    }
}