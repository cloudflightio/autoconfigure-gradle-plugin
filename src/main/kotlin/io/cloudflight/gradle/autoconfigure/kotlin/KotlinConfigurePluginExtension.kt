package io.cloudflight.gradle.autoconfigure.kotlin

import org.gradle.api.provider.Property

abstract class KotlinConfigurePluginExtension {

    abstract val kotlinVersion: Property<String>

}