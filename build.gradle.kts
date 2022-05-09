plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    id("maven-publish")
    alias(libs.plugins.nexus.publishing)
    id("signing")
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

gradlePlugin {
    isAutomatedPublishing = false // disable normal plugin publish configuration as long as we publish on MavenCentral
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
            description = "Used to configure a java project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin"
        }
        create("report-configure") {
            id = "io.cloudflight.autoconfigure.report-configure"
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/cloudflightio/autoconfigure-gradle-plugin")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                inceptionYear.set("2022")
                organization {
                    name.set("Cloudflight GmbH")
                    url.set("https://cloudflight.io")
                }
                developers {
                    developer {
                        id.set("cgrabmann")
                        name.set("Clemens Grabmann")
                        email.set("clemens.grabmann@cloudflight.io")
                    }
                }
                scm {
                    connection.set("scm:ggit@github.com:cloudflightio/autoconfigure-gradle-plugin.git")
                    developerConnection.set("scm:git@github.com:cloudflightio/autoconfigure-gradle-plugin.git")
                    url.set("https://github.com/cloudflightio/autoconfigure-gradle-plugin")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}

signing {
    setRequired {
        System.getenv("PGP_SECRET") != null
    }
    useInMemoryPgpKeys(System.getenv("PGP_SECRET"), System.getenv("PGP_PASSPHRASE"))
    sign(publishing.publications.getByName("maven"))
}
