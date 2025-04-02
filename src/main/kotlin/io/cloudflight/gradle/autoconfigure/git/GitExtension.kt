package io.cloudflight.gradle.autoconfigure.git

import com.gorylenko.GitPropertiesPlugin
import com.gorylenko.GitPropertiesPluginExtension
import org.gradle.api.Project

object GitExtension {

    fun create(project: Project) {
        project.plugins.apply(GitPropertiesPlugin::class.java)
    }

    fun configure(project: Project) {
        val gitProperties = project.extensions.getByType(GitPropertiesPluginExtension::class.java)
        gitProperties.customProperty("gradle.version", project.gradle.gradleVersion)
    }
}
