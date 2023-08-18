package io.cloudflight.gradle.autoconfigure.kotlin

import org.gradle.api.Project

internal fun isKotlinProject(project: Project): Boolean {
    val kotlinDirs = project.layout.projectDirectory.asFileTree.matching {
        it.include("src/*/kotlin/")
    }
    return !kotlinDirs.isEmpty
}
