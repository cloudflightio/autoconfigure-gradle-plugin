package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.gradle.autoconfigure.AutoConfigureExtension
import org.gradle.api.Project


internal fun isServerProject(project: Project): Boolean {
    val extension = project.rootProject.extensions.getByType(AutoConfigureExtension::class.java)

    return project.name.endsWith(extension.java.serverProjectSuffix.get())
}