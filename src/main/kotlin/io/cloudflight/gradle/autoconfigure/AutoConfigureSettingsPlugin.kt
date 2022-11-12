package io.cloudflight.gradle.autoconfigure

import io.cloudflight.ci.info.CI
import org.ajoberstar.grgit.gradle.GrgitService
import org.ajoberstar.reckon.core.Reckoner
import org.ajoberstar.reckon.core.Scope
import org.ajoberstar.reckon.core.VersionTagParser
import org.ajoberstar.reckon.gradle.ReckonExtension
import org.ajoberstar.reckon.gradle.ReckonSettingsPlugin
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.services.BuildServiceSpec
import org.slf4j.LoggerFactory
import java.util.*

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
            // then set the version number here.

            // when running without CI server, we are using the Reckoner Core to calculate
            // a new version based on the staging scheme, but then converting to a snapshot manually.
            // see https://github.com/cloudflightio/autoconfigure-gradle-plugin/issues/114 why we are doing this

            val grgitService = settings.gradle.sharedServices.registerIfAbsent(
                "reckon-grgit",
                GrgitService::class.java
            ) { spec: BuildServiceSpec<GrgitService.Params> ->
                spec.parameters.currentDirectory.set(settings.settingsDir)
                spec.parameters.initIfNotExists.set(false)
                spec.maxParallelUsages.set(1)
            }
            val git = grgitService.get().grgit
            val repo = git.repository.jgit.repository
            val reckoner = Reckoner.builder()
                .git(repo, VersionTagParser.getDefault())
                .scopeCalc { _ -> Optional.empty() }
                .stageCalc { _, _ -> Optional.empty() }
                .defaultInferredScope(Scope.PATCH)
                .stages("rc", "final")
                .build()
            val reckonedVersion = DefaultArtifactVersion(reckoner.reckon().toString())
            val developmentVersion =
                "${reckonedVersion.majorVersion}.${reckonedVersion.minorVersion}.${reckonedVersion.incrementalVersion}-SNAPSHOT"

            settings.gradle.allprojects { project ->
                project.version = developmentVersion
            }

            // analogous to the ReckonExtension.reckonVersion (which we don't call here on purpose)
            LOG.warn("Reckoned version for local development: $developmentVersion")
        }
        settings.gradle.projectsLoaded {
            it.rootProject.plugins.apply(AutoConfigureGradlePlugin::class.java)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AutoConfigureGradlePlugin::class.java)
    }
}