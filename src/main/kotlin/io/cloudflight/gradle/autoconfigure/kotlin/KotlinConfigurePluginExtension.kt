package io.cloudflight.gradle.autoconfigure.kotlin

import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion

abstract class KotlinConfigurePluginExtension {

    abstract val kotlinVersion: Property<String>

}