import org.ajoberstar.reckon.gradle.ReckonExtension

pluginManagement {
    repositories {
        maven {
            url = uri("https://artifacts.cloudflight.io/repository/plugins-maven")
        }
        mavenLocal() // REMOVE
    }
}

plugins {
    id("cloudflight-settings-plugin") version "8.0.4"
}

rootProject.name = "kotlin-springboot-angular"

configure<ReckonExtension> {
    setScopeCalc(calcScopeFromCommitMessages())
}

include("skeleton-api")
include("skeleton-server")
include("skeleton-ui")
