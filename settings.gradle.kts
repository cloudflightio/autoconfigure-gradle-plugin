plugins {
    id("io.cloudflight.autoconfigure-settings") version "1.1.2"
}

rootProject.name = "autoconfigure"

configure<org.ajoberstar.reckon.gradle.ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}
