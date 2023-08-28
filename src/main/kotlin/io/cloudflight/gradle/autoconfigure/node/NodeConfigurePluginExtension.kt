package io.cloudflight.gradle.autoconfigure.node

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File

abstract class NodeConfigurePluginExtension {

    abstract val nodeVersion: Property<String>
    abstract val downloadNode: Property<Boolean>

    abstract val npmVersion: Property<String>

    abstract val destinationDir: Property<File>
    abstract val inputFiles: ListProperty<Any>

}
