plugins {
    id("io.cloudflight.autoconfigure-settings") version "1.0.0"
}

rootProject.name = "autoconfigure"

configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}
