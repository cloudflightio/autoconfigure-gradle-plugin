package io.cloudflight.gradle.autoconfigure.swagger

import org.gradle.api.provider.Property

abstract class SwaggerCodegenConfigurePluginExtension {

    abstract val swaggerCodegenCliVersion: Property<String>
    abstract val nodeSwaggerGenerator: Property<String>

}