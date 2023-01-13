plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.18.0"
}

description = "A opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
group = "io.cloudflight.gradle"

autoConfigure {
    java {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendorName.set("Cloudflight")
    }
    kotlin {
        kotlinVersion.set("1.7.10")
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

    testImplementation(libs.bundles.testImplementationDependencies)

    testRuntimeOnly(libs.junit.engine)

    constraints {
        api(libs.jackson)
        api(libs.swagger.jersey2.jaxrs)
        api(libs.reflections)
        api(libs.commons.text) // https://securitylab.github.com/advisories/GHSL-2022-018_Apache_Commons_Text/
    }
}

tasks.compileTestKotlin.configure {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.io.path.ExperimentalPathApi"
    }
}

tasks.test {
    inputs.dir(layout.projectDirectory.dir("./src/test/fixtures"))
}

java {
    withJavadocJar()
}

tasks.withType<Test> {
    // we wanna set the java Launcher to 17 here in order to be able set higher java compatibility
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(17))
    })
}

pluginBundle {
    website = "https://github.com/cloudflightio/autoconfigure-gradle-plugin"
    vcsUrl = "https://github.com/cloudflightio/autoconfigure-gradle-plugin.git"
    tags = listOf("autoconfigure", "java", "kotlin")
}

gradlePlugin {
    plugins {
        create("autoconfigure-gradle") {
            id = "io.cloudflight.autoconfigure-gradle"
            displayName = "Autoconfigure-Gradle"
            description =
                "An opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
            implementationClass = "io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin"
        }
        create("autoconfigure-settings") {
            id = "io.cloudflight.autoconfigure-settings"
            displayName = "Autoconfigure-Settings"
            description =
                "An opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
            implementationClass = "io.cloudflight.gradle.autoconfigure.AutoConfigureSettingsPlugin"
        }
        create("java-configure") {
            id = "io.cloudflight.autoconfigure.java-configure"
            displayName = "Configure Java-Plugin"
            description = "Used to configure a java project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin"
        }
        create("kotlin-configure") {
            id = "io.cloudflight.autoconfigure.kotlin-configure"
            displayName = "Configure Kotlin-Plugin"
            description = "Used to configure a kotlin project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.kotlin.KotlinConfigurePlugin"
        }
        create("node-configure") {
            id = "io.cloudflight.autoconfigure.node-configure"
            displayName = "Configure the Node-Plugin"
            description = "Used to configure a node project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.node.NodeConfigurePlugin"
        }
        create("report-configure") {
            id = "io.cloudflight.autoconfigure.report-configure"
            displayName = "Configure global reports"
            description = "Preconfiguring reports for your build"
            implementationClass = "io.cloudflight.gradle.autoconfigure.report.ReportConfigurePlugin"
        }
        create("swagger-api-configure") {
            id = "io.cloudflight.autoconfigure.swagger-api-configure"
            displayName = "Configure Swagger API Generation"
            description = "Configure Swagger API Generation"
            implementationClass = "io.cloudflight.gradle.autoconfigure.swagger.SwaggerApiConfigurePlugin"
        }
        create("swagger-codegen-configure") {
            id = "io.cloudflight.autoconfigure.swagger-codegen-configure"
            displayName = "Configure Swagger Code Generation"
            description = "Configure Swagger Code Generation"
            implementationClass = "io.cloudflight.gradle.autoconfigure.swagger.SwaggerCodegenConfigurePlugin"
        }
    }
}

tasks.withType<Jar>() {
    from(layout.projectDirectory.file("LICENSE"))
    from(layout.projectDirectory.file("NOTICE"))
}