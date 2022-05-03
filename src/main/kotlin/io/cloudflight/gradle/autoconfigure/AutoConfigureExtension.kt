package io.cloudflight.gradle.autoconfigure

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

open class AutoConfigureExtension @Inject constructor(
        objects: ObjectFactory
) {

    val java: JavaAutoConfigurePluginExtension = objects.newInstance(JavaAutoConfigurePluginExtension::class.java)
    fun java(action: Action<JavaAutoConfigurePluginExtension>) {
        action.execute(java)
    }
}


abstract class JavaAutoConfigurePluginExtension {

    abstract val javaVersion: Property<JavaVersion>
    abstract val encoding: Property<String>
    abstract val serverProjectSuffix: Property<String>
    abstract val vendorName: Property<String>

}