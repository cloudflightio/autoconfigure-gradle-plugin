package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.java.archives.attributes
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaToolchainService

internal object PopulateManifestAction : Action<Task> {
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

    private const val GRADLE_VERSION = "Gradle-Version"
}