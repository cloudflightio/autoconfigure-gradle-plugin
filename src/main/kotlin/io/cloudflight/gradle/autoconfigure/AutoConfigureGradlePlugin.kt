package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.java.*
import io.cloudflight.gradle.autoconfigure.java.isJavaProject
import io.cloudflight.gradle.autoconfigure.util.isServerProject
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

class AutoConfigureGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) throw GradleException("'autoconfigure-gradle' plugin can only be applied to the root project.")

        val autoConfigure = target.extensions.create(EXTENSION_NAME, AutoConfigureExtension::class)
        with(autoConfigure.java) {
            javaVersion.convention(JAVA_VERSION)
            encoding.convention(JAVA_ENCODING)
            vendorName.convention(VENDOR_NAME)
            serverProjectSuffix.convention("-server")
        }

        for (project in target.allprojects) {
            applyPlugins(project, autoConfigure)
        }

    }

    companion object {
        const val EXTENSION_NAME = "autoConfigure"
    }

    private fun applyPlugins(project: Project, autoConfigure: AutoConfigureExtension) {
        val plugins = project.plugins

        if (isJavaProject(project)) {
            plugins.apply(JavaConfigurePlugin::class)
            val extension = project.extensions.getByType(JavaConfigurePluginExtension::class)
            val javaConfigure = autoConfigure.java
            extension.apply {
                javaVersion.set(javaConfigure.javaVersion)
                encoding.set(javaConfigure.encoding)
                vendorName.set(javaConfigure.vendorName)
                applicationBuild.set(javaConfigure.isServerProject(project))
            }
        }
    }
}