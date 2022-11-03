plugins {
    id("io.cloudflight.autoconfigure-settings") version "0.8.1"
}

configure<org.ajoberstar.reckon.gradle.ReckonExtension>() {
    setDefaultInferredScope("patch")
}

rootProject.name = "autoconfigure"

