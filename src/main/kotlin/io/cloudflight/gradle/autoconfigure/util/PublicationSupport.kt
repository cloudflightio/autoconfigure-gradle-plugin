package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.gradle.autoconfigure.swagger.SWAGGER_CLASSIFIER
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider

internal fun addApiDocumentationPublication(
    task: Task,
    artifacts: ArtifactHandler,
    targetDir: String,
    filename: String,
    format: String
): PublishArtifact {
    return artifacts.add(
        JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME,
        task.project.file("$targetDir/${filename}.${format}")
    ) {
        it.name = filename
        it.classifier = SWAGGER_CLASSIFIER
        it.type = format
        it.builtBy(task)
    }
}

internal fun addApiDocumentationPublication(
    project: Project,
    task: TaskProvider<out Task>,
    artifacts: ArtifactHandler,
    targetDir: String,
    basename: String,
    format: String
): PublishArtifact {
    return artifacts.add(
        JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME,
        project.file("$targetDir/${basename}.${format}")
    ) {
        it.name = basename
        it.classifier = SWAGGER_CLASSIFIER
        it.type = format
        it.builtBy(task.get())
    }
}