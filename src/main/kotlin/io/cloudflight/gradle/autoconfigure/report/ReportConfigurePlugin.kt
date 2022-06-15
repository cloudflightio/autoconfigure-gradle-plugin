package io.cloudflight.gradle.autoconfigure.report

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.reporting.ReportingExtension
import org.gradle.testing.jacoco.plugins.JacocoCoverageReport
import org.gradle.testing.jacoco.plugins.JacocoReportAggregationPlugin

open class ReportConfigurePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project != project.rootProject) throw GradleException("'report-configure' plugin can only be applied to the root project.")
        project.plugins.apply(JacocoReportAggregationPlugin::class.java)

        if (project.subprojects.isNotEmpty()) {
            val reporting = project.extensions.getByType(ReportingExtension::class.java)
            if (!reporting.reports.any { it.name == REPORT_TASK_NAME }) {
                reporting.reports.create(REPORT_TASK_NAME, JacocoCoverageReport::class.java) {
                    it.testType.set(TestSuiteType.UNIT_TEST)
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

    companion object {
        const val REPORT_TASK_NAME = "testCodeCoverageReport"
    }
}