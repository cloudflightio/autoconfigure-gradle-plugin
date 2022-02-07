package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.gradle.GRADLE_6_4
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.java.archives.attributes
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.tasks.named
import io.cloudflight.gradle.autoconfigure.extentions.kotlin.collections.contains
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.slf4j.LoggerFactory

private const val GRADLE_VERSION = "Gradle-Version"

private const val JUNIT_4_ARTIFACT_GROUP = "junit"
private const val JUNIT_PLATFORM_ARTIFACT_GROUP = "org.junit.jupiter"
private const val TESTNG_ARTIFACT_GROUP = "org.testng"

class JavaConfigurePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(JavaLibraryPlugin::class)

        val gradle = project.gradle
        val gradleVersion = DefaultArtifactVersion(gradle.gradleVersion)
        val extensions = project.extensions
        val tasks = project.tasks

        val javaConfigureExtension = extensions.create("javaConfigure", JavaConfigurePluginExtension::class)

        project.afterEvaluate {
            val javaPluginExtension = extensions.getByType(JavaPluginExtension::class)

            if (gradleVersion >= GRADLE_6_4) {
                javaPluginExtension.modularity.inferModulePath.set(true)
            }

            javaPluginExtension.sourceCompatibility = javaConfigureExtension.javaVersion.get()
            javaPluginExtension.targetCompatibility = javaConfigureExtension.javaVersion.get()

            if (!javaConfigureExtension.applicationBuild.get()) {
                javaPluginExtension.withSourcesJar()
            }

            val compileJava = tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile::class).get()
            compileJava.options.encoding = javaConfigureExtension.encoding.get()

            val compileTest = tasks.named(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME, JavaCompile::class).get()
            compileTest.options.encoding = javaConfigureExtension.encoding.get()

            val jar = tasks.named(JavaPlugin.JAR_TASK_NAME, Jar::class).get()
            jar.doFirst {
                val configuration = project.configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                val classpath = configuration.files.joinToString(" ") { it.name }
                val createdBy = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"

                jar.manifest.attributes(
                    "Class-Path" to classpath,
                    "Created-By" to createdBy,
                    "Implementation-Vendor" to javaConfigureExtension.vendorName.get(),
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    GRADLE_VERSION to gradle.gradleVersion
                )
            }

            val testTask = tasks.named(JavaPlugin.TEST_TASK_NAME, Test::class)
            testTask.configure {
                val testRuntimeDependencies =
                    project.configurations.getByName(JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                val useJunit =
                    testRuntimeDependencies.allDependencies.contains { dep -> dep.group == JUNIT_4_ARTIFACT_GROUP }
                val useJunitPlatform =
                    testRuntimeDependencies.allDependencies.contains { dep -> dep.group == JUNIT_PLATFORM_ARTIFACT_GROUP }
                val useTestNG =
                    testRuntimeDependencies.allDependencies.contains { dep -> dep.group == TESTNG_ARTIFACT_GROUP }

                if (arrayOf(useJunit, useJunitPlatform, useTestNG).filter { enabled -> enabled }.size > 1) {
                    LOG.warn("Multiple testing frameworks detected in runtime dependencies. No framework enabled automatically. junit4: $useJunit, junit5: $useJunitPlatform, testNg: $useTestNG")
                } else if(useJunit) {
                    it.useJUnit()
                    LOG.info("Enabled Junit4 as test platform")
                } else if(useJunitPlatform) {
                    it.useJUnitPlatform()
                    LOG.info("Enabled Junit5 as test platform")
                } else if(useTestNG) {
                    it.useTestNG()
                    LOG.info("Enabled TestNG as test platform")
                } else {
                    LOG.warn("No testing framework detected in runtime dependencies. No framework enabled automatically")
                }
            }

        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(JavaConfigurePlugin::class.java)
    }


}