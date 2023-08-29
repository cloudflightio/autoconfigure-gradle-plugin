plugins {
    id("io.cloudflight.autoconfigure-settings") version "0.11.1"
}

rootProject.name = "autoconfigure"

configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}
