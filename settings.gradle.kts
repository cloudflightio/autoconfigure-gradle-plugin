plugins {
    id("io.cloudflight.autoconfigure-settings") version "0.10.0-rc.1"
}

rootProject.name = "autoconfigure"

configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}
