package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.java.JavaAutoconfigurePlugin
import io.cloudflight.gradle.autoconfigure.java.isJavaProject
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoconfigureGradlePlugin: Plugin<Project> {
    override fun apply(parent: Project) {
        if (parent != parent.rootProject) throw GradleException("'autoconfigure-gradle' plugin can only be applied to the root project.")

        for (project in parent.allprojects) {
            applyPlugins(project)
        }
    }

    private fun applyPlugins(project: Project) {
        val plugins = project.plugins

        if (isJavaProject(project)) {
            plugins.apply(JavaAutoconfigurePlugin::class)
        }
    }
}
