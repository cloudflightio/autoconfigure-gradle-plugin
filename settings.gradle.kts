plugins {
    id("io.cloudflight.autoconfigure-settings") version "0.8.11"
}

rootProject.name = "autoconfigure"

configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}
