# Gradle AutoConfigure Plugin

[![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.cloudflight.autoconfigure-gradle?logo=gradle&label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/io.cloudflight.autoconfigure-gradle)

This plugin is an opinionated approach to configure a Gradle project automatically by convention. 
It supports to automatically configure various plugins to reduce boilerplate code in Gradle projects.

## Installation

You can apply that plugin via the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.cloudflight.autoconfigure-gradle)
by adding the following block to your `build.gradle.kts`:

````kotlin
plugins {
    id("io.cloudflight.autoconfigure-gradle") version "0.4.0"
}
````

## Requirements

This plugin requires at least Gradle 7.4 and Java 8.

## Main Concept

This plugin is meant to be applied only at the root module of your project - never on sub-modules if you have one.
All the plugin does by itself is scan the module and all sub-modules and apply
any of the internal plugins mentioned below depending on the layout. Instead of requiring you
to apply the same default over and over again, we do that automatically for you.


## Plugins

This plugin consists of multiple sub-plugins, all of which are being applied automatically when 
certain functionality is being detected (i.e. we automatically apply the Java Plugin when we detect 
a source folder `src/main/java`). 

### Java Plugin

If the plugin detects either the folder `src/main/java` or `src/test/java` in a module, it automatically
applies the `java-library` plugin and applies the following configuration:

#### Configuration

You can provide some configuration to this plugin by adding the following block to 
your `build.gradle.kts`:

````kotlin
javaConfigure {
    languageVersion.set(JavaLanguageVersion.of(17))
    vendorName.set("Cloudflight")
    applicationBuild.set(false)
    encoding.set("UTF-8")
}
````

The usage of those properties will be explained in the sections below:

#### Java Compatibility

Based on the `langageVersion` in your `JavaConfigurePluginExtension` we will set the [Java Toolchain](https://docs.gradle.org/current/userguide/toolchains.html)
for you and also use it in the Manifest, the default being Java 17. 

The encoding will be set automatically to all source sets, the default being UTF-8.

#### Unit-Test Configuration

The test task is automatically configured with `useJUnitPlatform()` to use the JUnit5 Platform per default. You do need to add the necessary dependencies yourself as described [here](https://docs.gradle.org/current/userguide/java_testing.html#compiling_and_executing_junit_jupiter_tests).

To override the used testing platform use:
```kotlin
tasks.test {
    useJUnit()
}
```

#### Jacoco

This plugin also automatically applies the [Jacoco-Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
in order to collect code-coverage information during our unit- and integration tests.

Call `gradle testCodeCoverageReport` to get a multi-module coverage report over all your modules in the
`build/reports/jacoco/testCodeCoverageReport/html` directory of your root module.

#### MANIFEST.MF

We automatically configure the task `jar.manifest` in order to create manifests like that:

````
Manifest-Version: 1.0
Class-Path: jsr305-3.0.2.jar
Created-By: 17.0.2 (Eclipse Adoptium)
Implementation-Vendor: your-vendor-name
Implementation-Title: my-module
Implementation-Version: 0.0.1
Gradle-Version: 7.3.3
````

* The `Class-Path` is created automatically from your `runtime` configuration
* `Gradle-Version` and `Created-By` are being taken from your current environment
* `Implementation-Version` is being set to the current `project.version`
* `Implementation-Title` is your `project.name`
* The `Implementation-Vendor` can be set by configuring the `vendorName` in the `JavaConfigurePluginExtension` (see above)

### Kotlin Plugin

If the plugin detects either the folder `src/main/kotlin` or `src/test/kotlin` in a module, it automatically
applies the [Kotlin Gradle Plugin](https://kotlinlang.org/docs/gradle.html) and applies the following configuration:

#### Configuration

You can provide some configuration to this plugin by adding the following block to
your `build.gradle.kts`:

````kotlin
kotlinConfigure {
    kotlinVersion.set("1.6.10")
}
````

The Kotlin plugin also automatically applies the Java Plugin with all its configuration possibilities. Use the configuration
there to adjust the JDK or the Manifest entries.

#### Kotlin Version

Per default, we are using the latest version of the Kotlin Plugin (1.7.0) to compile your Kotlin-Code. Still,
you can specify an older version (i.e. 1.5.20), which results in the following behaviour:

* The Kotlin StdLib of the version `1.5.20` is being added to the classpath
* The `apiVersion` and `languageVersion` is set to `1.5`, see [this link](https://kotlinlang.org/docs/gradle.html#attributes-common-to-jvm-and-js) for more details
* The setting `-Xjsr305=strict` is being added to the Kotlin Compiler options, see [this link](https://kotlinlang.org/docs/java-interop.html#jsr-305-support) for more details


## Auto-Configuration

In multi-module projects, each of the above mentioned plugins can be configured on a per-module basis,
but you can also use the `autoConfigure` extension of this plugin to set defaults for all submodules without
iterating through all modules by yourself.

To achieve that, add the following block to your root module `build.gradle.kts` in a multi-module project:

````kotlin
autoConfigure {
    java {
        languageVersion.set(JavaLanguageVersion.of(17))
        encoding.set("UTF-16")
        vendorName.set("My cool company")
    }
}
````

Each property here comes with an equivalent to the above mentioned sub-plugins. If you set a default value
on the root module here, and override it with a more specific value in the sub-module, then the latter will
always win.
