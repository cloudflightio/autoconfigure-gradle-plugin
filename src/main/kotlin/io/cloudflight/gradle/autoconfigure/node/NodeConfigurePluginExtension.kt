package io.cloudflight.gradle.autoconfigure.node

import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

abstract class NodeConfigurePluginExtension @Inject constructor(
    objects: ObjectFactory
) {

    abstract val nodeVersion: Property<String>
    abstract val downloadNode: Property<Boolean>

    val npm: NpmConfigurePluginExtension = objects.newInstance(NpmConfigurePluginExtension::class.java)
    fun npm(action: Action<NpmConfigurePluginExtension>) {
        action.execute(npm)
    }
}

abstract class NpmConfigurePluginExtension {

    abstract val npmVersion: Property<String>

    abstract val destinationDir: Property<File>
    abstract val inputFiles: ListProperty<Any>

}