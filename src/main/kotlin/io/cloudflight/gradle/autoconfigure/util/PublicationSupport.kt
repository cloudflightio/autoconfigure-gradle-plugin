package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.gradle.autoconfigure.springdoc.openapi.OpenApiFormat
import io.cloudflight.gradle.autoconfigure.swagger.SWAGGER_CLASSIFIER
import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
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
    task: TaskProvider<out Task>,
    artifacts: ArtifactHandler,
    targetDir: DirectoryProperty,
    basename: Provider<String>,
    format: Provider<OpenApiFormat>
): PublishArtifact {
    val fileName = basename.zip(format) { name, f ->
        "${name}.${f.extension}"
    }

    return artifacts.add(
        JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME,
        targetDir.file(fileName)
    ) {
        it.name = basename.get()
        it.classifier = SWAGGER_CLASSIFIER
        it.type = format.get().extension
        it.builtBy(task.get())
    }
}
