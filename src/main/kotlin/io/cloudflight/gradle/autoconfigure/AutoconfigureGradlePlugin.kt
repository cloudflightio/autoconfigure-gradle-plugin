package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.java.isJavaProject
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoconfigureGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) throw GradleException("'autoconfigure-gradle' plugin can only be applied to the root project.")

        getOrCreateExtensionOnRootProject(target)

        for (project in target.allprojects) {
            applyPlugins(project)
        }

    }

    companion object {
        const val EXTENSION_NAME = "autoConfigure"

        fun getOrCreateExtensionOnRootProject(project: Project): AutoConfigureExtension {
            val existing = project.rootProject.extensions.findByType(AutoConfigureExtension::class.java)
            if (existing != null) {
                return existing
            }
            return project.extensions.create(EXTENSION_NAME, AutoConfigureExtension::class)
        }
    }

    private fun applyPlugins(project: Project) {
        val plugins = project.plugins

        if (isJavaProject(project)) {
            plugins.apply(JavaConfigurePlugin::class)
        }
    }
}
