package io.cloudflight.gradle.autoconfigure.swagger

import org.gradle.api.Project
import java.io.File

internal data class SwaggerApiDescriptor(
    val swaggerName:String,
    val swaggerProject: Project?,
    val swaggerPath: File?
)

internal const val SWAGGER_CLASSIFIER = "swagger"
internal const val YAML = "yaml"
