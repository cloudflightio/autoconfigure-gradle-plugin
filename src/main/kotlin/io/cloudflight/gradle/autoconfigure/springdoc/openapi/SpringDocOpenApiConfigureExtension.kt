package io.cloudflight.gradle.autoconfigure.springdoc.openapi

import org.gradle.api.provider.Property

enum class OpenApiFormat(val extension: String) {
    YAML("yaml"),
    JSON("json")
}

abstract class SpringDocOpenApiConfigureExtension {
    abstract val fileFormat: Property<OpenApiFormat>
}
