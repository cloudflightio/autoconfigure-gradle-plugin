package io.cloudflight.gradle.autoconfigure.java

import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion

abstract class JavaConfigurePluginExtension {

    /**
     * If `true`, the sourcesJar and javadoc will also be generated for this module.
     * This default is `true`
     */
    abstract val createSourceArtifacts: Property<Boolean>

    /**
     * If this property is true on the given module, we are applying additional plugins based on the setting of the
     * property [applicationFramework].
     *
     * This is `true` per default for all modules which end with `-server`
     */
    abstract val applicationBuild: Property<Boolean>

    /**
     * Depending on the value of this property we apply the underlying framework plugin (the spring boot plugin for
     * Spring Boot and the Shadow-Plugin for Micronaut)
     */
    abstract val applicationFramework: Property<ApplicationFramework>

    /**
     * If this property is set to true, then the application framework plugins (Spring, Micronaut) are only applied
     * on CI environments. This may make sense in order to reduce build logic for local development
     */
    abstract val applyApplicationFrameworkOnlyOnCI: Property<Boolean>

    /**
     * The version of JDK that is being used to compile Java and Kotlin code.
     * This default is 17.
     */
    abstract val languageVersion: Property<JavaLanguageVersion>

    /**
     * The encoding for all sourcesets (main and test), the default is `UTF-8`
     */
    abstract val encoding: Property<String>

    /**
     * The `vendorName` is being used as `Implementation-Vendor` inside the `MANIFEST.MF`
     */
    abstract val vendorName: Property<String>

}