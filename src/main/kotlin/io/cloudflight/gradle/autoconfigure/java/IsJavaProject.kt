package io.cloudflight.gradle.autoconfigure.java

import org.gradle.api.Project

internal fun isJavaProject(project: Project): Boolean {
    return project.layout.projectDirectory.dir("src/main/java/").asFile.exists() ||
            project.layout.projectDirectory.dir("src/test/java/").asFile.exists()
}