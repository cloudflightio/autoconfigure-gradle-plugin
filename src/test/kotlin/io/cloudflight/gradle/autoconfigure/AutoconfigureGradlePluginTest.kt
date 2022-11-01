package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.java.JAVA_LANGUAGE_VERSION
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

    @Test
    fun `build execution times are reported correctly`(): Unit =
        autoconfigureFixture("multi-module") {
            val result = runCleanBuild()
            // it's hard to automatically test formatting of the build execution times, because
            // the build times change from run to run (and therefore the content of the log), but we
            // print the output here on purpose to be able to have a look at that manually
            println(result.normalizedOutput)
            assertThat(result.normalizedOutput).contains("BUILD EXECUTION TIMES")
        }

    @ParameterizedTest
    @MethodSource("singleJavaModuleArguments")
    fun `JavaConfigurePlugin is applied to a single-module java project and configured options are respected`(options: TestOptions): Unit =
        autoconfigureFixture(options.fixtureName) {
            val result = runTasks()

            println(result.normalizedOutput)

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

            println(result.normalizedOutput)

            assertThat(result.normalizedOutput)
                .contains("Auto-applied JavaConfigurePlugin to java-module")
                .doesNotContain("Auto-applied JavaConfigurePlugin to no-plugin-applied")

            println(result.normalizedOutput)
            assertThat(result.normalizedOutput).contains("BUILD EXECUTION TIMES")
        }

    @Test
    fun `in a multi module project the Autoconfigure plugin automatically propagates the root project version to the subprojects if they don't define it themselves`(): Unit =
        autoconfigureFixture("multi-module") {
            runCleanBuild()
            val javaModule = "java-module"
            val javaModuleJar = this.buildDir(javaModule).resolve("libs/${javaModule}-1.0.0.jar")
            val kotlinModule = "kotlin-module"
            val kotlinModuleJar = this.buildDir(kotlinModule).resolve("libs/${kotlinModule}-1.1.0.jar")

            assertThat(javaModuleJar).exists()
            assertThat(kotlinModuleJar).exists()
        }

    @Test
    fun `in a multi module project also the root project can be a java project`(): Unit =
        autoconfigureFixture("multi-module-with-root") {
            val result = runTasks()
            assertThat(result.normalizedOutput).contains("BUILD EXECUTION TIMES")
        }

    @Test
    fun `in a multi module project also the root project can be a java project and the plugin can be applied`(): Unit =
        autoconfigureFixture("multi-module-with-root-and-plugin-applied") {
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
                        languageVersion = JAVA_LANGUAGE_VERSION.asInt(),
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

private fun <T : Any> autoconfigureFixture(
    fixtureName: String,
    gradleVersion: String? = null,
    environment: Map<String, String> = emptyMap(),
    testWork: ProjectFixture .() -> T
): T =
    useFixture("autoconfigure", fixtureName, gradleVersion, environment, testWork)
