plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.18.0"
}

description = "A opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
group = "io.cloudflight.gradle"
if (System.getenv("RELEASE") != "true") {
    version = "$version-SNAPSHOT"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.maven.artifact)
    implementation(libs.kotlin.logging)

    implementation(libs.kotlin.allopen)
    implementation(libs.kotlin.gradleplugin)
    implementation(libs.kotlin.noarg)

    testImplementation(libs.bundles.testImplementationDependencies)

    testRuntimeOnly(libs.junit.engine)
}

tasks.compileTestKotlin.configure {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlin.io.path.ExperimentalPathApi"
    }
}

tasks.test {
    useJUnitPlatform()
    inputs.dir(layout.projectDirectory.dir("./src/test/fixtures"))
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
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
                "A opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
            implementationClass = "io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin"
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
        create("report-configure") {
            id = "io.cloudflight.autoconfigure.report-configure"
            displayName = "Configure global reports"
            description = "Preconfiguring reports for your build"
            implementationClass = "io.cloudflight.gradle.autoconfigure.report.ReportConfigurePlugin"
        }
    }
}

tasks.withType<Jar>() {
    manifest {
        val compiler  = javaToolchains.compilerFor(java.toolchain).get().metadata
        val createdBy = compiler.javaRuntimeVersion + " (" + compiler.vendor + ")"
        val vendorName: String by project.extra

        attributes(
            "Created-By" to createdBy,
            "Implementation-Vendor" to vendorName,
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
        )
    }

    from(layout.projectDirectory.file("LICENSE"))
    from(layout.projectDirectory.file("NOTICE"))
}