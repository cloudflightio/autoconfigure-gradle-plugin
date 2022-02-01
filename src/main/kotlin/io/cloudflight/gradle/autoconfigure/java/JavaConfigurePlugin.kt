package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.gradle.GRADLE_6_4
import io.cloudflight.gradle.autoconfigure.gradle.api.java.archives.attributes
import io.cloudflight.gradle.autoconfigure.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.gradle.api.tasks.named
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

private const val GRADLE_VERSION = "Gradle-Version"

class JavaConfigurePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(JavaLibraryPlugin::class)

        val gradle = project.gradle
        val gradleVersion = DefaultArtifactVersion(gradle.gradleVersion)
        val parent = project.parent
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
        }
    }
}