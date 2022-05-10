package io.cloudflight.gradle.autoconfigure

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion
import javax.inject.Inject

open class AutoConfigureExtension @Inject constructor(
    objects: ObjectFactory
) {

    val java: JavaAutoConfigurePluginExtension = objects.newInstance(JavaAutoConfigurePluginExtension::class.java)
    fun java(action: Action<JavaAutoConfigurePluginExtension>) {
        action.execute(java)
    }

    val kotlin: KotlinAutoConfigurePluginExtension = objects.newInstance(KotlinAutoConfigurePluginExtension::class.java)
    fun kotlin(action: Action<KotlinAutoConfigurePluginExtension>) {
        action.execute(kotlin)
    }
}


abstract class JavaAutoConfigurePluginExtension {

    abstract val languageVersion: Property<JavaLanguageVersion>
    abstract val encoding: Property<String>
    abstract val serverProjectSuffix: Property<String>
    abstract val vendorName: Property<String>

}

abstract class KotlinAutoConfigurePluginExtension {
    abstract val kotlinVersion: Property<String>
}