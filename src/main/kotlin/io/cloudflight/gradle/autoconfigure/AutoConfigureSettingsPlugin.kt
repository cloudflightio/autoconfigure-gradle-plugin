package io.cloudflight.gradle.autoconfigure

import io.cloudflight.ci.info.CI
import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonSettingsPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class AutoConfigureSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        if (CI.isCI) {
            settings.plugins.apply(ReckonSettingsPlugin::class.java)
            settings.extensions.getByType(ReckonExtension::class.java).apply {
                stages("rc", "final")
                setStageCalc(calcStageFromProp())
                setScopeCalc(calcScopeFromProp())
                setDefaultInferredScope("patch")
            }
        } else if (!settings.plugins.hasPlugin(ReckonSettingsPlugin::class.java)) {
            // if the ReckonSettingsPlugin hasn't yet been applied by the user,
            // (i.e. it is intended that it is applied also locally)
            // then set the version number here
            settings.gradle.allprojects { project ->
                project.version = "1.0.0-SNAPSHOT"
            }
        }
        settings.gradle.projectsLoaded {
            it.rootProject.plugins.apply(AutoConfigureGradlePlugin::class.java)
        }
    }
}