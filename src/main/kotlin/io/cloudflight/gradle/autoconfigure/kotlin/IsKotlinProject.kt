package io.cloudflight.gradle.autoconfigure.kotlin

import org.gradle.api.Project

internal fun isKotlinProject(project: Project): Boolean {
    return project.layout.projectDirectory.dir("src/main/kotlin/").asFile.exists() ||
            project.layout.projectDirectory.dir("src/test/kotlin/").asFile.exists()
}