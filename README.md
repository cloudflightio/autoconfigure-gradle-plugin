# Gradle AutoConfigure Plugin

[![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.cloudflight.autoconfigure-gradle?logo=gradle&label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/io.cloudflight.autoconfigure-gradle)

This plugin is an opinionated approach to configure a Gradle project automatically by convention. 
It supports to automatically configure various plugins to reduce boilerplate code in Gradle projects.

As an example, instead of applying the Kotlin Plugin manually, the AutoConfigure-Plugin does that automatically for you
when we detect a folder `src/main/kotlin` inside your module, and we also automatically configure the Kotlin
plugin with sensible defaults (encoding, JVM runtime).

The idea is similar to Spring Boot, where beans are being auto-generated and auto-configured based on the current classpath.

## Requirements

This plugin requires at least Gradle 7.4 and Java 17. You can still configure this plugin to use Java 8 (or any other Java version)
to compile your codebase.

## Installation

There are two different ways how to apply the AutoConfigure plugin:

1. As normal project plugin inside `build.gradle` or `build.gradle.kts`
2. As settings-plugin inside `settings.gradle` or `settings.gradle.kts`

Where the latter approach has the advantage that we also include the awesome [Reckon-Plugin](https://github.com/ajoberstar/reckon) 
for semantic versioning

### Install as project plugin

You can apply that plugin via the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/io.cloudflight.autoconfigure-gradle)
by adding the following block to your `build.gradle.kts`:

````kotlin
plugins {
    id("io.cloudflight.autoconfigure-gradle") version "1.0.1"
}
````

This plugin is meant to be applied only at the root module of your project - never on sub-modules if you have one.
All the plugin does by itself is scan the module and all sub-modules and apply
any of the internal plugins mentioned below depending on the layout. Instead of requiring you
to apply the same default over and over again, we do that automatically for you.

### Install as settings plugin

Alternatively, you can also apply the AutoConfigure-Plugin as Settings plugin in your `settings.gradle.kts`:

````kotlin
plugins {
    id("io.cloudflight.autoconfigure-settings") version "1.0.1"
}
````

In case your [code is running on a CI-Server](https://github.com/cloudflightio/ci-info), we will then automatically apply the [Reckon-Plugin](https://github.com/ajoberstar/reckon) for you in 
and pre-configure it with the following defaults:

````groovy
reckon {
    stages('rc', 'final')
    scopeCalc = calcScopeFromProp()
    stageCalc = calcStageFromProp()
    defaultInferredScope = 'patch'
}
````

If this is fine for you, you don't need to add anything to your `settings.gradle`, but feel free to override
this with any of your desired values.

In any way, you can omit the `version` property from your `build.gradle` file then, Reckon will take care of it.

If you are not running on a CI-Server (i.e. you are doing local development), the Reckon-Plugin is NOT applied, instead
we set the version to the `-SNAPSHOT` or the last reckoned version as described in [this ticket](https://github.com/cloudflightio/autoconfigure-gradle-plugin/issues/114) (but you're free to override that in the `build.gradle` temporarily). 
That means if the Reckon plugin would create `0.8.5-rc.0.0+20221111T075331Z`, we convert that to `0.8.5-SNAPSHOT` locally. The reason for this behaviour is [this ticket](https://github.com/ajoberstar/reckon/issues/189).

In any case, when applying the Settings-Plugin, we will also automatically apply the `io.cloudflight.autoconfigure-gradle` plugin for you, so you don't need
to do that on your own. 


## Samples

A good starting point to understand how this plugin works might be to go through our [samples of real-life applications](samples/samples.md).

## Plugins

This plugin consists of multiple sub-plugins, all of which are being applied automatically when 
certain functionality is being detected (i.e. we automatically apply the Java Plugin when we detect 
a source folder `src/main/java`). 

### Java Plugin

If the plugin detects either the folder `src/main/java` or `src/test/java` in a module, it automatically
applies the `java` plugin. If you need the `java-library` plugin in a specific module
you can apply it by adding the following to the module's `build.gradle.kts`:
```kotlin
plugins{
    id("java-library")
}
```

The `java` plugin applies the following configuration:

#### Configuration

You can provide some configuration to this plugin by adding the following block to 
your `build.gradle.kts`:

````kotlin
javaConfigure {
    languageVersion.set(JavaLanguageVersion.of(17))
    vendorName.set("Cloudflight")
    applicationBuild.set(false)
    encoding.set("UTF-8")
    createSourceArtifacts.set(false)
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

#### Artifacts

Per default the source and javadoc jars will be generated for all builds that have the property `applicationBuild` set to `false`.   
If you want to change that behavior you can manually set the `createSourceArtifacts` depending on the desired outcome.

### Test Fixtures

If the folder `src/testFixtures` exists, we automatically apply the plugin [`java-test-fixtures`](https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures),
you don't need to apply it on your own.

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

Per default, we are using the latest version of the Kotlin Plugin (2.0.0) to compile your Kotlin-Code. Still,
you can specify an older version (i.e. 1.5.20), which results in the following behaviour:

* The Kotlin StdLib of the version `1.5.20` is being added to the classpath
* The `apiVersion` and `languageVersion` is set to `1.5`, see [this link](https://kotlinlang.org/docs/gradle.html#attributes-common-to-jvm-and-js) for more details
* The setting `-Xjsr305=strict` is being added to the Kotlin Compiler options, see [this link](https://kotlinlang.org/docs/java-interop.html#jsr-305-support) for more details

## Node-Plugin

Apply the plugin `io.cloudflight.autoconfigure.node-configure` in order to configure the [Gradle Node Plugin](https://github.com/node-gradle/gradle-node-plugin)

````kotlin
plugins {
    id("io.cloudflight.autoconfigure.node-configure")
}
````

You can configure this plugin with the `NodeConfigurePluginExtension` as follows:

````kotlin
nodeConfigure {
    nodeVersion = "16.15.1"
    downloadNode = true
    npmVersion = "6.14.10"
    inputFiles = [
        fileTree('node_modules'),
        fileTree('src'),
        file('package.json'),
        file('tsconfig.json')
    ]    
}
````

These values shown here are also the default values, so you can omit them, i.e. if you're fine with this values simply don't configure anything.

You then automatically get the following tasks:

| Task         | Description                                                                                                                                   |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `clfNpmBuild` | Runs `npm run build`. Automatically being called during compilation.                                                                          |
| `clfNpmBuildDev` | Runs `npm run build:dev`. Use this target to have live reloading supported.                                                                   |
| `clfNpmLint` | Runs `npm run lint`. Automatically being called in the check-phase (right after compilation).                                                 |
| `clfNpmTest` | Runs `npm run test`. This task is automatically being attached to the `test` target, if your `package.json` contains a `test` script.         |
| `clfNpmUpdateVersion` | Updates the version in your `package.json` to the current value in `build.gradle`. This task is only being called in CI-Servers automatically |

The Node-Configure-Plugin takes care about dependencies between this tasks, as well as inputs and outputs and up-to-date-handling.

## Swagger Plugin

When it comes to the definition of APIs over HTTP, Swagger/OpenAPI is the defacto-standard nowadays. Code generation is an important part there, and there exist a couple of libraries out there, to generate code from or to OpenAPI specifications.

This plugin preconfigures both the  for API generation [Swagger Gradle Plugin](https://github.com/gigaSproule/swagger-gradle-plugin)
and the [Swagger Generator Plugin](https://github.com/int128/gradle-swagger-generator-plugin) for code generation. 

While both of them are really powerful and flexible, they all have some downsides:

* They resolve dependencies too early and do not play well with the Java Platform Plugin for central dependency constraints.
* It’s not easy to configure the plugins in a way to have the task order applied in the correct way (i.e. which task is being dependent on what when it comes to code generation)
* Their dynamic configuration makes it hard to provide sensible defaults.
* They are coming with a lot of transitive dependencies, some of them incompatible with important other plugins like the Spring Boot plugin.

With some extra-code within this plugin we managed to find a solution that fullfils all these 
requirements while keeping the flexibility of the underlying plugins.

In order to support all possibible use cases, there is one important thing to understand: 
Swagger/OpenAPI specifictions (yaml or json) always need to be in another module than the code that is being generated out of it.

So we always see the whole setup as two steps:

1. Manage your API specifications (either by generating API spcifications from code, or by importing external specifications)
2. Generate code from Swagger/OpenAPI


### Manage your API specifications

#### Generate from code

Our recommended way to work with OpenAPI clients is to write APIs interfaces and DTOs in 
Java or Kotlin code, annotate them with Spring and OpenAPI annotations and then generate the 
OpenAPI specification from there, and use that in turn to generate other clients like for Typescript.

The module `skeleton-api` only contains Java/Kotlin interfaces along with their DTOs and all 
Spring MVC annotations to define the API contract to that server. 
The according `build.gradle` file looks like that:

````groovy
plugins {
    id "io.cloudflight.autoconfigure.swagger-api-configure"
}

dependencies {
    implementation 'io.swagger:swagger-annotations'
    implementation 'org.springframework:spring-web'
}
````

By doing this, the build will generate API definitions in JSON and YAML format to the directory `skeleton-api/build/generated-resources/openapi`.
The task which is doing this, is called `clfGenerateSwaggerDocumentation` and it is triggered 
automatically before calling the `jar` task.

This task wraps the `GenerateSwaggerDocsTask` of the plugin mentioned above and configures it automatically with those values:

````groovy
swagger {
    apiSource {
        locations = [
            project.group + '.api',
            project.group + '.api.dto'
        ]
    }
}
````

That means, if the groupId of your project (to be set in the root `build.gradle`) 
is `io.cloudflight.skeleton`, then your API interfaces need to be in the package `io.cloudflight.skeleton.api` (or a subpackage of it) 
and your DTOs in the package `io.cloudflight.skeleton.api.dto`.

Additionally, the Autoconfigure Gradle Plugin will create an additional maven publication, 
that means the YAML and JSON files are also being published to your artifact repository like Nexus 
and you can fetch it from there like JAR files.


#### API from file

In case you have to embed an external OpenAPI specification, or for any reason you want to 
maintain your own OpenAPI specification instead of writing code, you can do that as well.

In order to take part in the advanced dependency resolution that have been implemented 
within this plugin on top of the official Swagger plugin, you have to put those OpenAPI 
files into a separate gradle/maven module:

Your module structure might look like that:

````
externalsystem-api
  build.gradle
  myopenapifile.yml
myproject-server
  build.gradle
myproject-ui
  package.json
  build.gradle
````

We are focussing on the module `externalsystem-api` now, the `build.gradle` looks as follows:

````groovy
apply plugin: 'java'  

artifacts.add(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME, file('myopenapifile.yml')) {   
    name project.name
    classifier 'swagger'
    type 'yaml' 
}
````

* We need to apply the Java plugin manually here, as we don’t have a folder `src/main/java` here. 
* Refer to your OpenAPI or Swagger configuration file and create a publication. 
* If you OpenAPI or Swagger configuration file is provided in JSON format, you can alternatively specify json as type here.

That’s it. Your OpenAPI specification (Whereever it comes from) is now registered and can be used later as reference for code generation.

### Generate code from Swagger/OpenAPI

Now that we have defined our API modules, we can use them to generate code from it. To achieve that, simply apply the 
plugin `io.cloudflight.autoconfigure.swagger-codegen-configure` and create a dependency within the configuration 
`swaggerApi` to your API module.

This plugin applies the necessary swaggerSources configuration for the [Swagger Generator Plugin](https://github.com/int128/gradle-swagger-generator-plugin) 
so that you don't have to do anything extra. The minimal configuration is nothing more than that:

````groovy
plugins {
    id 'io.cloudflight.autoconfigure.swagger-codegen-configure'
}

dependencies {
    swaggerApi project(':api')
}
````

Dependending of the layout of the module which applies `io.cloudflight.autoconfigure.swagger-codegen-configure`, code is
being generated with the following defaults (the rules are evaluated top down, first match is being applied):


| Rule                            | `swaggerGenerator`   | `swaggerLibrary` |
|---------------------------------|----------------------|------------------|
| The Node plugin is applied      | `typescript-angular` | `null`           |
| Module name ends with `-client` | `spring`             | `spring-cloud`   |
| Default                         | `spring`             | `spring-boot`    |

Target packages are derived from the project `group`.

In rare occasions the applied defaults do not work, in that case you can simply add or overwrite the 
missing configuration as you would if you were using the [Swagger Generator Plugin](https://github.com/int128/gradle-swagger-generator-plugin) without the 
AutoConfigure Plugin:

````groovy
swaggerSources {
    'externalsystem-api' { 
        code {
            additionalProperties = [
                'delegatePattern' : 'true'
            ]
        }
    }
}
````

Please note that the configuration name (`externalsystem-api` in that case) has to match the name of the referenced module or dependency.

A more complex configuration would be:

````groovy
swaggerSources {
    'hub-api-definition' {
        code {
            language = "java"
            library = "resttemplate"
            components = [apis: true, apiTests: false, supportingFiles: true]
            additionalProperties = [
                'invokerPackage': "io.cloudflight.commons.hub",
                'apiPackage'  : "io.cloudflight.commons.hub.client",
                'modelPackage'  : "io.cloudflight.commons.hub.dto"
            ]
        }
    }
}
````

#### Exchanging the generator CLI

Per default, we are using the module `io.swagger.codegen.v3:swagger-codegen-cli:3.0.34` as code generator library.

There are two ways to override that:

1. override the property `swaggerCodegenCliVersion` in your `build.gradle` to use another version of `io.swagger.codegen.v3:swagger-codegen-cli`.
````groovy
swaggerCodgenConfigure {
    swaggerCodegenCliVersion = "3.0.30"
}
````
2. define your own generator within the `swaggerCodegen` as described in the [official plugin documentation](https://github.com/int128/gradle-swagger-generator-plugin#code-generation).

#### Setting the generator for node projects

`typescript-angular` is the default generator for node projects. This is not what you want in case you are using other frameworks.

To change the generator, override the property `nodeSwaggerGenerator` in your `build.gradle`.
````groovy
swaggerCodgenConfigure {
    nodeSwaggerGenerator = "typescript-fetch"
}
````

#### Adding custom templates

You can also define your own artifacts containg mustache templates for code generation, simply add a dependency within the `swaggerTemplate` configuration
and refer to a JAR that contains the updates mustache files:

````groovy
dependencies {
    swaggerApi project(':api')

    swaggerTemplate "<your-jar-with-mustache-templates>"
}
````

## SpringDoc OpenApi plugin

This autoconfigures the Gradle plugin provided by the [SpringDoc OpenApi](https://github.com/springdoc/springdoc-openapi-gradle-plugin) project.

SpringDoc OpenApi has a different approach than the above explained swagger plugin.
It needs a running Spring Boot application that serves the OpenAPI document.

The autoconfiguration for this plugin is applied as follows:

```groovy
plugins {
    id 'io.cloudflight.autoconfigure.springdoc-openapi-configure'
}
```

The plugin has to be applied to a module that provides a Spring Boot application which the plugin will try to start using a custom Spring Boot run configuration.

The springdoc plugin is automatically configured to generate the open-api spec in `YAML` format. If you prefer the `JSON` format you can easily change that by using our extension:
```groovy
import io.cloudflight.gradle.autoconfigure.springdoc.openapi.OpenApiFormat

openApiConfigure {
    fileFormat = OpenApiFormat.JSON
}
```

For generating the OpenAPI document the task `clfGenerateOpenApiDocumentation` has to be run.

### Grouped Apis

In case you are using [grouped api configuration](https://springdoc.org/faq.html#_how_can_i_define_multiple_openapi_definitions_in_one_spring_boot_project) in your project, instead of something like this (as described in [springdoc gradle plugin](https://github.com/springdoc/springdoc-openapi-gradle-plugin#customization))

```groovy
openApi {
    groupedApiMappings = [
            "http://localhost:8080/v3/api-docs/groupA": "groupA.yaml",
            "http://localhost:8080/v3/api-docs/groupB": "groupB.yaml"
    ]
}
```

you have to use the configuration provided by this plugin, since the port of the spring application will be randomly selected

```groovy
openApiConfigure {
    groupedApiMappings = [
            "/v3/api-docs/groupA": "groupA.yaml",
            "/v3/api-docs/groupB": "groupB.yaml"
    ]
}
```

To provide other custom configuration just add the openApi extension configuration block.
See https://github.com/springdoc/springdoc-openapi-gradle-plugin#customization.

```groovy
openApi {
    outputDir=buildDir
}
```

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
    kotlin {
        kotlinVersion.set("1.5.31")
    }
}
````

Each property here comes with an equivalent to the above mentioned sub-plugins. If you set a default value
on the root module here, and override it with a more specific value in the sub-module, then the latter will
always win.

### Version and Group

To reduce unnecessary code for setting the version and group of all sub-modules we automatically set the version and group
of all sub-modules to the values defined for the root-module.   
If you want to have a different version or group, just define it in the sub-module. Any values defined in the `build.gradle` of a sub-module will not be overridden.
```kotlin
version = "1.1.1"
group = "com.project.module.other.group"
```

## Contributing

-   [Contributing to the project](CONTRIBUTING.md)
