package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getValue
import org.gradle.api.Project


internal const val SERVER_PROJECT_SUFFIX = "serverProjectSuffix"

internal fun isServerProject(project: Project): Boolean {
    val serverProjectSuffix: String = project.extensions.extraProperties.getValue(SERVER_PROJECT_SUFFIX)
    return project.name.endsWith(serverProjectSuffix)
}