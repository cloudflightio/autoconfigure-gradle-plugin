package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.normalizedOutput
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.jar.Attributes.Name
import java.util.jar.Manifest
import java.util.stream.Stream
import kotlin.io.path.inputStream

data class TestOptions(
    val fixtureName: String,
    val languageVersion: Int,
    val encoding: String,
    val testPlatformMessage: String,
    val createsSourceJar: Boolean,
    val implementationVendor: String,
    val inferModulePath: Boolean,
    val gradleVersion: String? = null,
    val checkConfigurationInTestOutput: Boolean = true
)

class JavaConfigurePluginTest {

    private fun Int.toJavaVersion(): String {
        return if (this == 8) {
            "1.8"
        } else {
            this.toString()
        }
    }

    @ParameterizedTest
    @MethodSource("singleJavaModuleArguments")
    fun `the supplied options are used to configure the JavaPlugin`(
        options: TestOptions
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion) {
        val result = runCleanBuild()
        if (options.checkConfigurationInTestOutput) {
            assertThat(result.normalizedOutput).contains(
                """
                javaPluginExtension.modularity.inferModulePath: ${options.inferModulePath}
                javaPluginExtension.sourceCompatibility: ${options.languageVersion.toJavaVersion()}
                javaPluginExtension.targetCompatibility: ${options.languageVersion.toJavaVersion()}
                compileJava.options.encoding: ${options.encoding}
            """.trimIndent()
            ).contains(
                """
                compileTestJava.options.encoding: ${options.encoding}
            """.trimIndent()
            ).contains(options.testPlatformMessage)
        }

        val outJarDirPath = fixtureDir.resolve("build/libs")
        val outJarLibPath = outJarDirPath.resolve("$fixtureName-1.0.0.jar")
        assertThat(outJarLibPath).exists().isRegularFile()

        val outJarSourcesPath = outJarDirPath.resolve("$fixtureName-1.0.0-sources.jar")
        if (options.createsSourceJar) {
            assertThat(outJarSourcesPath).exists().isRegularFile()
        } else {
            assertThat(outJarSourcesPath).doesNotExist()
        }

        val manifestPath = fixtureDir.resolve("build/tmp/jar/MANIFEST.MF")
        val manifest = Manifest(manifestPath.inputStream()).mainAttributes
        assertThat(manifest)
            .containsEntry(Name.CLASS_PATH, "")
            .containsEntry(Name.IMPLEMENTATION_VENDOR, options.implementationVendor)
            .containsEntry(Name.IMPLEMENTATION_TITLE, fixtureName)
            .containsEntry(Name.IMPLEMENTATION_VERSION, "1.0.0")
    }

    companion object {

        @JvmStatic
        fun singleJavaModuleArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-library",
                        languageVersion = 8,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit5 as test platform",
                        createsSourceJar = true,
                        implementationVendor = "Cloudflight XYZ",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit5 as test platform",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default-kts",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "Runs the module with Kotlin DSL",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true,
                        checkConfigurationInTestOutput = false
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default-kts-configure",
                        languageVersion = 8,
                        encoding = "UTF-8",
                        testPlatformMessage = "Runs the module with Kotlin DSL",
                        createsSourceJar = true,
                        implementationVendor = "Cloudflight",
                        inferModulePath = true,
                        checkConfigurationInTestOutput = false
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-application",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit5 as test platform",
                        createsSourceJar = false,
                        implementationVendor = "Cloudflight Test Vendor",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-junit4",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit4 as test platform",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-testNG",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled TestNG as test platform",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-junit4&testNG",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "Multiple testing frameworks detected in runtime dependencies.",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-no-test-framework",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "No testing framework detected in runtime dependencies.",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                )
            )
        }
    }
}

private val JAVA_FIXTURE_PATH = Paths.get("java")
private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture(JAVA_FIXTURE_PATH, fixtureName, gradleVersion, testWork)