package io.cloudflight.gradle.autoconfigure

import io.cloudflight.ci.info.CI
import org.ajoberstar.reckon.core.Version
import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonSettingsPlugin
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.provider.Provider
import org.slf4j.LoggerFactory

class AutoConfigureSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.plugins.apply(ReckonSettingsPlugin::class.java)
        val reckonExtension = settings.extensions.getByType(ReckonExtension::class.java).apply {
            stages("rc", "final")
            setStageCalc(calcStageFromProp())
            setScopeCalc(calcScopeFromProp())
            setDefaultInferredScope("patch")
        }

        if (!CI.isCI) {
            // when we are not running on a CI server, we are using the reckon plugin to calculate
            // just as the user has configured it, but then converting the version to a snapshot manually.
            // see https://github.com/cloudflightio/autoconfigure-gradle-plugin/issues/114 why we are doing this.

            // we are therefore overriding the version again which has been set in the ReckonSettingsPlugin
            // with another provider which simply adds the -SNAPSHOT at the end.
            val sharedVersion = DelayedLocalSnapshotVersion(reckonExtension.version)
            settings.gradle.allprojects { prj: Project ->
                prj.version = sharedVersion
            }
        }
        settings.gradle.projectsLoaded {
            it.rootProject.plugins.apply(AutoConfigureGradlePlugin::class.java)
        }
    }

    private class DelayedLocalSnapshotVersion(versionProvider: Provider<Version>) {

        private val developmentVersion: String by lazy {
            val rv = DefaultArtifactVersion(versionProvider.get().toString())
            val v = "${rv.majorVersion}.${rv.minorVersion}.${rv.incrementalVersion}-SNAPSHOT"
            LOG.warn("Overriding reckoned version for local development to $v")
            v
        }

        override fun toString(): String {
            return developmentVersion
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AutoConfigureGradlePlugin::class.java)
    }
}
