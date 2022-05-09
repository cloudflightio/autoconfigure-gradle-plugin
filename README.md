# Gradle AutoConfigure Plugin

[![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.cloudflight.gradle/autoconfigure.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.cloudflight.gradle/autoconfigure)

This plugin is an opinionated approach to configure a Gradle project automatically by convention. 
It supports to automatically configure various plugins to reduce boilerplate code in Gradle projects.

## Installation

This plugin is not yet published to the [Gradle Plugin Portal](https://plugins.gradle.org/), that's 
why you need to apply it via a `buildscript` block in your `build.gradle.kts`:

````kotlin
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.cloudflight.gradle:autoconfigure:0.1.4")
    }
}

apply(plugin = "io.cloudflight.autoconfigure-gradle")
````

## Requirements

This plugin requires at least Gradle 7.2 and Java 8.

## Main Concept

This plugin is meant to be applied only at the root module of your project - never on sub-modules if you have one. It will
automatically. All the plugin does by itself is scan the module and all sub-modules and apply
any of the internal plugins mentioned below depending on the layout. Instead of requiring you
to apply the same default over and over again, we do that automatically for you.


## Plugins

This plugin consists of multiple sub-plugins, all of which are being applied automatically when 
certain functionality is being detected (i.e. we automatically apply the Java Plugin when we detect 
a source folder `src/main/java`). 

### Java Plugin

If the plugin detects either to folder `src/main/java` or `src/test/java` in a module, it automatically
applies the `java-library` plugin and applies the following configuration:

#### Configuration

You can provide some configuration to this plugin by adding the following block to 
your `build.gradle.kts`:

````kotlin
configure<JavaConfigurePluginExtension> {
    languageVersion.set(JavaLanguageVersion.of(11))
    vendorName.set("Cloudflight")
    applicationBuild.set(false)
    encoding.set("UTF-8")
}
````

or if you prefer the Groovy Version in `build.gradle`:
````groovy
javaConfigure {
    languageVersion.set(JavaLanguageVersion.of(11))
    vendorName.set("Cloudflight")
    applicationBuild.set(false)
    encoding.set("UTF-8")
}
````


The usage of those properties will be explained in the sections below:

#### Java Compatibility

Based on the `langageVersion` in your `JavaConfigurePluginExtension` we will set the [Java Toolchain](https://docs.gradle.org/current/userguide/toolchains.html)
for you and also use it in the Manifest, the default being Java 11. 

The encoding will be set automatically to all source sets, the default being UTF-8.

#### Unit-Test Configuration

Based on your `testImplementation` dependencies, we automatically configure
the `test` section of that module:

| dependency            | automatically applied configuration |
|-----------------------|-------------------------------------|
| `junit.*`             | `test { useJUnit() }`               |
| `org.junit.jupiter.*` | `test { useJUnitPlatform() }`       |
| `org.testng.*`        | `test { useTestNG() }`              |

In other words, if you have `org.junit.jupiter:junit-jupiter-engine:5.8.2` on your `testImplemenation`
classpath, we automatically apply `test { useJUnitPlatform() }` for you.


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

## Auto-Configuration

In multi-module projects, each of the above mentioned plugins can be configured on a per-module basis,
but you can also use the `autoConfigure` extension of this plugin to set defaults for all submodules without
iterating through all modules by yourself.

To achieve that, add the following block to your root module `build.gradle` in a multi-module project:

````groovy
autoConfigure {
    java {
        languageVersion = JavaLanguageVersion.of(17)
        encoding = "UTF-16"
        vendorName = "My cool company"
    }
}
````

Each property here comes with an equivalent to the above mentioned sub-plugins. If you set a default value
on the root module here, and override it with a more specific value in the sub-module, then the latter will
always win.