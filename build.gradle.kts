plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.1.0"
}

description = "An opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
group = "io.cloudflight.gradle"

autoConfigure {
    java {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
        vendorName.set("Cloudflight")
    }
    kotlin {
        kotlinVersion.set(libs.versions.kotlin.get())
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.license.plugin)

    implementation(libs.maven.artifact)

    implementation(libs.kotlin.allopen)
    implementation(libs.kotlin.gradleplugin)
    implementation(libs.kotlin.noarg)

    implementation(libs.git.properties.plugin)
    implementation(libs.spring.boot.plugin)
    implementation(libs.shadow.plugin)

    implementation(libs.reckon.plugin)

    implementation(libs.node.plugin)
    implementation(libs.json.wrapper)
    implementation(libs.ci.info)

    implementation(libs.swagger.gradle.plugin)
    implementation(libs.swagger.codegen.plugin)

    implementation(libs.springdoc.openapi.plugin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.exec.fork.plugin)

    testImplementation(libs.bundles.testImplementationDependencies)

    testRuntimeOnly(libs.junit.engine)

    constraints {
        api(libs.jackson)
        api(libs.swagger.jersey2.jaxrs)
        api(libs.reflections)
        api(libs.commons.text)
    }
}

tasks.compileTestKotlin.configure {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.io.path.ExperimentalPathApi"
    }
}

java {
    withJavadocJar()
}

tasks.withType<Test> {
    inputs.files(layout.projectDirectory.asFileTree.matching {
        include("src/test/fixtures/")
        exclude("**/.gradle/", "**/build")
    })

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    })
}

gradlePlugin {
    website.set("https://github.com/cloudflightio/autoconfigure-gradle-plugin")
    vcsUrl.set("https://github.com/cloudflightio/autoconfigure-gradle-plugin.git")
    plugins {
        create("autoconfigure-gradle") {
            id = "io.cloudflight.autoconfigure-gradle"
            displayName = "Autoconfigure-Gradle"
            description =
                "An opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
            implementationClass = "io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin"
            tags.set(listOf("autoconfigure", "java", "kotlin"))
        }
        create("autoconfigure-settings") {
            id = "io.cloudflight.autoconfigure-settings"
            displayName = "Autoconfigure-Settings"
            description =
                "An opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
            implementationClass = "io.cloudflight.gradle.autoconfigure.AutoConfigureSettingsPlugin"
            tags.set(listOf("autoconfigure", "java", "kotlin"))
        }
        create("java-configure") {
            id = "io.cloudflight.autoconfigure.java-configure"
            displayName = "Configure Java-Plugin"
            description = "Used to configure a java project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin"
            tags.set(listOf("autoconfigure", "java"))
        }
        create("kotlin-configure") {
            id = "io.cloudflight.autoconfigure.kotlin-configure"
            displayName = "Configure Kotlin-Plugin"
            description = "Used to configure a kotlin project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.kotlin.KotlinConfigurePlugin"
            tags.set(listOf("autoconfigure", "kotlin"))
        }
        create("node-configure") {
            id = "io.cloudflight.autoconfigure.node-configure"
            displayName = "Configure the Node-Plugin"
            description = "Used to configure a node project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.node.NodeConfigurePlugin"
            tags.set(listOf("autoconfigure", "node", "nodejs"))
        }
        create("report-configure") {
            id = "io.cloudflight.autoconfigure.report-configure"
            displayName = "Configure global reports"
            description = "Preconfiguring reports for your build"
            implementationClass = "io.cloudflight.gradle.autoconfigure.report.ReportConfigurePlugin"
            tags.set(listOf("autoconfigure", "report"))
        }
        create("swagger-api-configure") {
            id = "io.cloudflight.autoconfigure.swagger-api-configure"
            displayName = "Configure Swagger API Generation"
            description = "Configure Swagger API Generation"
            implementationClass = "io.cloudflight.gradle.autoconfigure.swagger.SwaggerApiConfigurePlugin"
            tags.set(listOf("autoconfigure", "swagger", "api"))
        }
        create("swagger-codegen-configure") {
            id = "io.cloudflight.autoconfigure.swagger-codegen-configure"
            displayName = "Configure Swagger Code Generation"
            description = "Configure Swagger Code Generation"
            implementationClass = "io.cloudflight.gradle.autoconfigure.swagger.SwaggerCodegenConfigurePlugin"
            tags.set(listOf("autoconfigure", "swagger", "codegen"))
        }
        create("springdoc-openapi-configure") {
            id = "io.cloudflight.autoconfigure.springdoc-openapi-configure"
            displayName = "Configure SpringDoc OpenApi Generation"
            description = "Configure SpringDoc OpenApi Generation"
            implementationClass = "io.cloudflight.gradle.autoconfigure.springdoc.openapi.SpringDocOpenApiConfigurePlugin"
            tags.set(listOf("springdoc", "openapi", "api"))
        }
    }
}

tasks.withType<Jar> {
    from(layout.projectDirectory.file("LICENSE"))
    from(layout.projectDirectory.file("NOTICE"))
}
