package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.gradle.autoconfigure.JavaAutoConfigurePluginExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider


internal fun JavaAutoConfigurePluginExtension.isServerProject(project: Project): Provider<Boolean> {
    return this.serverProjectSuffix.map {
        project.name.endsWith(it)
    }
}