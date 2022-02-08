package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getValue
import io.cloudflight.gradle.autoconfigure.util.isServerProject
import io.cloudflight.gradle.autoconfigure.util.loadDefaults
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaAutoconfigurePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val defaultResource = javaClass.getResource("/java/defaults.properties")!!
        loadDefaults(project, defaultResource)

        val plugins = project.plugins
        val extensions = project.extensions
        val extraProperties = extensions.extraProperties

        plugins.apply(JavaConfigurePlugin::class)
        val javaConfigurePluginExtension = extensions.getByType(JavaConfigurePluginExtension::class)

        javaConfigurePluginExtension.javaVersion(extraProperties.getValue(JAVA_VERSION))
        javaConfigurePluginExtension.applicationBuild.set(isServerProject(project))
        javaConfigurePluginExtension.encoding.set(extraProperties.getValue<String>(ENCODING))
    }

    companion object {
        internal const val JAVA_VERSION = "javaVersion"
        internal const val ENCODING = "encoding"
    }
}