package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.normalizedOutput
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class AutoConfigureSettingsPluginTest {

    @ParameterizedTest
    @MethodSource("autoConfigureSettingsArguments")
    fun `AutoConfigureSettings plugin is applied correctly`(options: TestOptions): Unit =
        autoConfigureSettingsFixture(options.fixtureName, environment = CI_ENV) {
            val result = runCleanBuild()
            assertThat(result.normalizedOutput).contains("Reckoned version")
            assertThat(
                buildDir().resolve("libs").toFile()
                    .listFiles { _, name -> name.contains("unspecified") }
            ).isEmpty()
        }

    @Test
    fun `print version number on CI server`(): Unit =
        autoConfigureSettingsFixture("single-java-module-default", environment = CI_ENV) {
            val result = run("-q", "clfPrintVersion", infoLoggerEnabled = false)
            // we are just checking that there is just one line here, the content itself is determined by the reckon plugin
            assertThat(result.normalizedOutput.trim())
                .hasLineCount(1)
                .doesNotEndWith("-SNAPSHOT")
        }

    @Test
    fun `print version number locally`(): Unit =
        autoConfigureSettingsFixture("single-java-module-default") {
            val result = run("-q", "clfPrintVersion", infoLoggerEnabled = false)
            assertThat(result.normalizedOutput.trim())
                .hasLineCount(1)
                .endsWith("-SNAPSHOT")
        }

    companion object {
        @JvmStatic
        fun autoConfigureSettingsArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-autoconfigure-override"
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default"
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-reckon-override"
                    )
                )
            )
        }

        val CI_ENV = mapOf("GITLAB_CI" to "true")
    }

    data class TestOptions(
        val fixtureName: String
    )

    private fun <T : Any> autoConfigureSettingsFixture(
        fixtureName: String,
        gradleVersion: String? = null,
        environment: Map<String, String> = emptyMap(),
        testWork: ProjectFixture .() -> T
    ): T =
        useFixture("autoconfigure-settings", fixtureName, gradleVersion, environment, testWork)
}


