plugins {
    id("io.cloudflight.autoconfigure.java-configure")
}

repositories {
    mavenCentral()
}

description = "Cloudflight Gradle Test"
group = "io.cloudflight.gradle"
version = "1.0.0"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}