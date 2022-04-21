plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-gradle-plugin")
    id("maven-publish")
    alias(libs.plugins.nexus.publishing)
    id("signing")
}

group = "io.cloudflight.gradle"

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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

gradlePlugin {
    plugins {
        create("autoconfigure-gradle") {
            id = "io.cloudflight.autoconfigure-gradle"
            displayName = "Autoconfigure-Gradle"
            description =
                "A opinionated approach to configure a gradle project automatically by convention. It supports to automatically configure various plugins to reduce boilerplate code in gradle projects."
            implementationClass = "io.cloudflight.gradle.autoconfigure.AutoconfigureGradlePlugin"
        }
        create("java-configure") {
            id = "io.cloudflight.autoconfigure.java-configure"
            description = "Used by 'io.cloudflight.autoconfigure.java-autoconfigure' to configure a java project."
            implementationClass = "io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin"
        }
        create("java-autoconfigure") {
            id = "io.cloudflight.autoconfigure.java-autoconfigure"
            description =
                "Used by 'io.cloudflight.autoconfigure-gradle' to configure a java project by naming conventions and configurations from 'gradle.properties'"
            implementationClass = "io.cloudflight.gradle.autoconfigure.java.JavaAutoconfigurePlugin"
        }
    }
}

tasks.withType<Jar>() {
    manifest {
        val createdBy = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"
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
