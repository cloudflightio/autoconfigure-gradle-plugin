package io.cloudflight.gradle.autoconfigure.node

import org.gradle.api.Project

fun isNodeProject(project: Project): Boolean {
    return project.file(NpmHelper.PACKAGE_JSON).exists()
}