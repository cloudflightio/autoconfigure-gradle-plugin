package io.cloudflight.gradle.autoconfigure.java

import org.gradle.api.JavaVersion
import org.gradle.api.provider.Property

abstract class JavaConfigurePluginExtension {

    abstract val applicationBuild: Property<Boolean>
    abstract val javaVersion: Property<JavaVersion>
    abstract val encoding: Property<String>

    abstract val vendorName: Property<String>

    init {
        applicationBuild.convention(false)
        javaVersion.convention(JavaVersion.toVersion(System.getProperty("java.version")))
        encoding.convention("UTF-8")
        vendorName.convention("")
    }

}