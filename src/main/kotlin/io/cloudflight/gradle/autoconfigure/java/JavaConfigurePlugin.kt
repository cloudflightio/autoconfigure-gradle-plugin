package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.java.archives.attributes
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.tasks.named
import io.cloudflight.gradle.autoconfigure.extentions.kotlin.collections.contains
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.jetbrains.kotlin.gradle.plugin.statistics.ReportStatisticsToElasticSearch.user
import org.slf4j.LoggerFactory

private const val GRADLE_VERSION = "Gradle-Version"

private const val JUNIT_4_ARTIFACT_GROUP = "junit"
private const val JUNIT_PLATFORM_ARTIFACT_GROUP = "org.junit.jupiter"
private const val TESTNG_ARTIFACT_GROUP = "org.testng"

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
        }

        val javaPluginExtension = extensions.getByType(JavaPluginExtension::class)
        javaPluginExtension.modularity.inferModulePath.set(true)
        javaPluginExtension.toolchain.languageVersion.set(javaConfigureExtension.languageVersion)

        tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class).configure {
            it.doFirst(PopulateManifestAction)
        }

        tasks.named(JavaPlugin.TEST_TASK_NAME, Test::class).configure {
            it.doFirst(ConfigureUnitTestFrameworkAction)
        }

        project.afterEvaluate {

            if (!javaConfigureExtension.applicationBuild.get()) {
                javaPluginExtension.withSourcesJar()
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
    }

    private object ConfigureUnitTestFrameworkAction : Action<Task> {
        override fun execute(t: Task) {
            val test = t as Test
            val testRuntimeDependencies =
                t.project.configurations.getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
            val useJunit =
                testRuntimeDependencies.allDependencies.contains { dep -> dep.group == JUNIT_4_ARTIFACT_GROUP }
            val useJunitPlatform =
                testRuntimeDependencies.allDependencies.contains { dep -> dep.group == JUNIT_PLATFORM_ARTIFACT_GROUP }
            val useTestNG =
                testRuntimeDependencies.allDependencies.contains { dep -> dep.group == TESTNG_ARTIFACT_GROUP }

            if (arrayOf(useJunit, useJunitPlatform, useTestNG).filter { enabled -> enabled }.size > 1) {
                LOG.warn("Multiple testing frameworks detected in runtime dependencies. No framework enabled automatically. junit4: $useJunit, junit5: $useJunitPlatform, testNg: $useTestNG")
            } else if (useJunit) {
                test.useJUnit()
                LOG.info("Enabled Junit4 as test platform")
            } else if (useJunitPlatform) {
                test.useJUnitPlatform()
                LOG.info("Enabled Junit5 as test platform")
            } else if (useTestNG) {
                test.useTestNG()
                LOG.info("Enabled TestNG as test platform")
            } else {
                LOG.warn("No testing framework detected in runtime dependencies. No framework enabled automatically")
            }
        }
    }

    private object PopulateManifestAction : Action<Task> {
        override fun execute(t: Task) {
            val jar = t as Jar
            val project = t.project
            val javaConfigureExtension = project.extensions.getByType(JavaConfigurePluginExtension::class)
            val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class)
            val configuration =
                project.configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME)
            val classpath = configuration.files.joinToString(" ") { it.name }
            val javaToolChains = project.extensions.getByType(JavaToolchainService::class.java)
            val compiler = javaToolChains.compilerFor(javaPluginExtension.toolchain).get().metadata
            val createdBy = compiler.javaRuntimeVersion + " (" + compiler.vendor + ")"

            jar.manifest.attributes(
                "Class-Path" to classpath,
                "Created-By" to createdBy,
                "Implementation-Vendor" to javaConfigureExtension.vendorName.get(),
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                GRADLE_VERSION to project.gradle.gradleVersion
            )
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(JavaConfigurePlugin::class.java)

        const val EXTENSION_NAME = "javaConfigure"
    }
}