package io.cloudflight.gradle.autoconfigure

import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonSettingsPlugin
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class AutoConfigureSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.plugins.apply(ReckonSettingsPlugin::class.java)
        settings.extensions.getByType(ReckonExtension::class.java).apply {
            stages("rc", "final")
            setStageCalc(calcStageFromProp())
            setScopeCalc(calcScopeFromProp())
            setDefaultInferredScope("patch")
        }
        settings.gradle.projectsLoaded {
            it.rootProject.plugins.apply(AutoConfigureGradlePlugin::class.java)
        }
    }
}