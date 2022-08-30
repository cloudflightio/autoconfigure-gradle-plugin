package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.java.*
import io.cloudflight.gradle.autoconfigure.kotlin.KotlinConfigurePlugin
import io.cloudflight.gradle.autoconfigure.kotlin.KotlinConfigurePluginExtension
import io.cloudflight.gradle.autoconfigure.kotlin.isKotlinProject
import io.cloudflight.gradle.autoconfigure.node.NodeConfigurePlugin
import io.cloudflight.gradle.autoconfigure.node.isNodeProject
import io.cloudflight.gradle.autoconfigure.report.ReportConfigurePlugin
import io.cloudflight.gradle.autoconfigure.util.BuildExecutionTimeListener
import io.cloudflight.gradle.autoconfigure.util.isServerProject
import io.cloudflight.license.gradle.LicensePlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginContainer
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.slf4j.LoggerFactory

class AutoConfigureGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) throw GradleException("'autoconfigure-gradle' plugin can only be applied to the root project.")

        if (!target.gradle.startParameter.isParallelProjectExecutionEnabled) {
            target.gradle.addListener(BuildExecutionTimeListener())
        } else {
            target.logger.info("Parallel builds are enabled. Build Execution Times are not calculated as this is not yet supported")
        }

        val autoConfigure = target.extensions.create(EXTENSION_NAME, AutoConfigureExtension::class)
        with(autoConfigure.java) {
            languageVersion.convention(JAVA_LANGUAGE_VERSION)
            encoding.convention(JAVA_ENCODING)
            vendorName.convention(VENDOR_NAME)
            serverProjectSuffix.convention("-server")
        }
        with(autoConfigure.kotlin) {
            kotlinVersion.convention(target.getKotlinPluginVersion())
        }

        for (project in target.allprojects) {
            applyPlugins(project, autoConfigure)
        }

        target.plugins.apply(ReportConfigurePlugin::class.java)

        // Setting the group and version of all sub-modules here still allows the user to override it in the sub-modules build.gradle directly.
        // This is because afterEvaluate of the root-module runs directly after the build.gradle of the root-module finished and
        // the build.gradle of a sub-module is evaluated
        target.afterEvaluate {
            for (child in target.subprojects) {
                child.group = target.group
                child.version = target.version
            }
        }
    }

    companion object {
        const val EXTENSION_NAME = "autoConfigure"
        const val TASK_GROUP = "cloudflight"
    }

    private fun applyPlugins(project: Project, autoConfigure: AutoConfigureExtension) {
        val plugins = project.plugins

        if (isJavaProject(project)) {
            applyJava(plugins, project, autoConfigure)
        }

        if (isKotlinProject(project)) {
            applyJava(plugins, project, autoConfigure)
            applyKotlin(plugins, project, autoConfigure)
        }

        if (isNodeProject(project)) {
            applyJava(plugins, project, autoConfigure)
            applyNode(project)
        }

        if (isJavaProject(project) || isKotlinProject(project) || isNodeProject(project)) {
            project.plugins.apply(LicensePlugin::class.java)
        }
    }

    private fun applyKotlin(
        plugins: PluginContainer,
        project: Project,
        autoConfigure: AutoConfigureExtension
    ) {
        plugins.apply(KotlinConfigurePlugin::class)
        val extension = project.extensions.getByType(KotlinConfigurePluginExtension::class)
        val kotlinConfigure = autoConfigure.kotlin
        extension.apply {
            kotlinVersion.set(kotlinConfigure.kotlinVersion)
        }
    }

    private fun applyJava(
        plugins: PluginContainer,
        project: Project,
        autoConfigure: AutoConfigureExtension
    ) {
        plugins.apply(JavaConfigurePlugin::class)
        val extension = project.extensions.getByType(JavaConfigurePluginExtension::class)
        val javaConfigure = autoConfigure.java
        extension.apply {
            languageVersion.set(javaConfigure.languageVersion)
            encoding.set(javaConfigure.encoding)
            vendorName.set(javaConfigure.vendorName)
            applicationBuild.set(javaConfigure.isServerProject(project))
        }
    }

    private fun applyNode(project: Project) {
        project.plugins.apply(NodeConfigurePlugin::class)
    }
}
