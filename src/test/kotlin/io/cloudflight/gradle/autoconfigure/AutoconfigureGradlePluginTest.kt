package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.normalizedOutput
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

data class TestOptions(
    val fixtureName: String,
    val languageVersion: Int,
    val encoding: String,
    val vendorName: String,
    val applicationBuild: Boolean
)

class AutoconfigureGradlePluginTest {

    @Test
    fun `JavaConfigurePlugin is not applied to a non java project`(): Unit = autoconfigureFixture("no-autoconfig") {
        val result = runTasks()

        assertThat(result.normalizedOutput).doesNotContain(JavaConfigurePlugin::class.simpleName)
    }

    @ParameterizedTest
    @MethodSource("singleJavaModuleArguments")
    fun `JavaConfigurePlugin is applied to a single-module java project and configured options are respected`(options: TestOptions): Unit =
        autoconfigureFixture(options.fixtureName) {
            val result = runTasks()

            assertThat(result.normalizedOutput).contains(JavaConfigurePlugin::class.simpleName).contains(
                """
                javaConfigurePluginExtension.languageVersion: ${options.languageVersion}
                javaConfigurePluginExtension.encoding: ${options.encoding}
                javaConfigurePluginExtension.vendorName: ${options.vendorName}
                javaConfigurePluginExtension.applicationBuild: ${options.applicationBuild}
            """.trimIndent()
            )
        }

    @Test
    fun `in a multi module project the JavaConfigurePlugin is only applied to java project`(): Unit =
        autoconfigureFixture("multi-module") {
            val result = runTasks()

            val startIndexNoPluginApplied = result.normalizedOutput.indexOf("Project no-plugin-applied start")
            val endIndexNoPluginApplied =
                result.normalizedOutput.indexOf("Project no-plugin-applied stop", startIndexNoPluginApplied)
            val noPluginAppliedOutput =
                result.normalizedOutput.substring(startIndexNoPluginApplied, endIndexNoPluginApplied)

            val startIndexJavaModule = result.normalizedOutput.indexOf("Project java-module start")
            val endIndexJavaModule = result.normalizedOutput.indexOf("Project java-module stop", startIndexJavaModule)
            val javaModuleOutput = result.normalizedOutput.substring(startIndexJavaModule, endIndexJavaModule)

            assertThat(noPluginAppliedOutput).doesNotContain(JavaConfigurePlugin::class.simpleName)
            assertThat(javaModuleOutput).contains(JavaConfigurePlugin::class.simpleName)
            assertThat(result.normalizedOutput).contains("BUILD EXECUTION TIMES")
        }

    @Test
    fun `in a multi module project also the root project can be a java project`(): Unit =
        autoconfigureFixture("multi-module-with-root") {
            val result = runTasks()
            assertThat(result.normalizedOutput).contains("BUILD EXECUTION TIMES")
        }

    companion object {
        @JvmStatic
        fun singleJavaModuleArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        vendorName = "",
                        applicationBuild = false
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-autoconfigure",
                        languageVersion = 16,
                        encoding = "UTF-16",
                        vendorName = "Cloudflight XYZ",
                        applicationBuild = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-configure",
                        languageVersion = 16,
                        encoding = "UTF-16",
                        vendorName = "Cloudflight XYZ",
                        applicationBuild = true
                    )
                )
            )
        }
    }

}

private val AUTOCONFIGURE_FIXTURE_PATH = Paths.get("autoconfigure")
private fun <T : Any> autoconfigureFixture(
    fixtureName: String,
    gradleVersion: String? = null,
    environment: Map<String, String> = emptyMap(),
    testWork: ProjectFixture .() -> T
): T =
    useFixture(AUTOCONFIGURE_FIXTURE_PATH, fixtureName, gradleVersion, environment, testWork)
