package io.cloudflight.gradle.autoconfigure.report

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

data class TestOptions(
    val fixtureName: String,
    val gradleVersion: String? = null
)

class ReportConfigurePluginTest {

    @ParameterizedTest
    @MethodSource("reportModuleArguments")
    fun `the supplied options are used to configure the ReportPlugin`(
        options: TestOptions
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion) {
        val result = run("clean", "build", ReportConfigurePlugin.REPORT_TASK_NAME)

        assertThat(result).isNotNull

        val reportDirPath = buildDir().resolve("reports/jacoco/testCodeCoverageReport/html")

        val htmlReportPath = reportDirPath.resolve("index.html")
        assertThat(htmlReportPath).exists().isRegularFile()

        val fooClassReport = reportDirPath.resolve("io.cloudflight.gradle/Foo.html")
        assertThat(fooClassReport).exists().isRegularFile()
    }

    companion object {

        @JvmStatic
        fun reportModuleArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "multi-module-jacoco"
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-module-jacoco"
                    )
                )
            )
        }
    }
}

private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture("report", fixtureName, gradleVersion, emptyMap(), testWork)
