package io.cloudflight.gradle.autoconfigure.report

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin

@Suppress("UnstableApiUsage")
open class ReportConfigurePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project != project.rootProject) throw GradleException("'report-configure' plugin can only be applied to the root project.")
        project.plugins.apply(JacocoReportAggregationPlugin::class.java)

        // we need to do this block in afterEvaluate as it can be that additional plugins are being applied
        // in the build.gradle file at a later point of time which in turn apply the JavaLibraryPlugin and
        // that wants to create its own test spec
        project.afterEvaluate {
            if (project.subprojects.isNotEmpty()) {
                val reporting = project.extensions.getByType(ReportingExtension::class.java)
                if (!reporting.reports.any { it.name == REPORT_TASK_NAME }) {
                    reporting.reports.create(REPORT_TASK_NAME, JacocoCoverageReport::class.java) {
                        it.testSuiteName.set("test")
                    }

                    for (subproject in project.subprojects) {
                        project.dependencies.add(
                            JacocoReportAggregationPlugin.JACOCO_AGGREGATION_CONFIGURATION_NAME,
                            subproject
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val REPORT_TASK_NAME = "testCodeCoverageReport"
    }
}
