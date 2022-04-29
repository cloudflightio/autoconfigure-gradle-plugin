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
        classpath("io.cloudflight.gradle:autoconfigure:0.1.2")
    }
}

apply(plugin = "io.cloudflight.autoconfigure-gradle")
````

## Java Plugin

If the plugin detects either to folder `src/main/java` or `src/test/java`, it automatically
applies the `java-library` plugin and applies the following configuration:

### Configuration

You can provide some configuratoin to this plugin by adding the following block to 
your `build.gradle.kts`:

````kotlin
configure<JavaConfigurePluginExtension> {
    javaVersion.set(JavaVersion.VERSION_11)
    vendorName.set("Cloudflight")
    applicationBuild.set(false)
    encoding.set("UTF-8")
}
````

The usage of those properties will be explained in the sections below:

### Java Compatibility

TBD

### Unit-Test Configuration

TBD

### MANIFEST.MF

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


