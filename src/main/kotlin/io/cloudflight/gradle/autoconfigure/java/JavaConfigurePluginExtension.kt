package io.cloudflight.gradle.autoconfigure.java

import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion

abstract class JavaConfigurePluginExtension {

    abstract val applicationBuild: Property<Boolean>
    abstract val languageVersion: Property<JavaLanguageVersion>
    abstract val encoding: Property<String>
    abstract val vendorName: Property<String>

}