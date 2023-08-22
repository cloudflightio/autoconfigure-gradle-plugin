import org.ajoberstar.reckon.gradle.ReckonExtension

plugins {
    id("io.cloudflight.autoconfigure-settings")
}

rootProject.name = "kotlin-springboot-angular"

configure<ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}

include("skeleton-api")
include("skeleton-server")
include("skeleton-ui")
