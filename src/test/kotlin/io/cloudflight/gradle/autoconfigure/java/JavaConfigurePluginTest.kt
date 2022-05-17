package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.normalizedOutput
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import io.cloudflight.gradle.autoconfigure.util.EnvironmentUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.Attributes.Name
import java.util.jar.Manifest
import java.util.stream.Stream
import kotlin.io.path.inputStream

data class TestOptions(
    val fixtureName: String,
    val languageVersion: Int,
    val encoding: String,
    val createsSourceJar: Boolean,
    val hasVersionSuffixOnJar: Boolean = true,
    val implementationVendor: String,
    val inferModulePath: Boolean,
    val successfulTestCount: Int? = null,
    val gradleVersion: String? = null,
    val environment:Map<String, String> = emptyMap(),
    val checkConfigurationInTestOutput: Boolean = true,
    val classpath: String = "",
    val additionalChecks: ((fixtureDir: Path) -> (Unit))? = null
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
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion, options.environment) {
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
            )
        }

        if (options.successfulTestCount != null) {
            assertThat(result.normalizedOutput).contains("SUCCESS: Executed ${options.successfulTestCount} tests")
        }

        val outJarDirPath = fixtureDir.resolve("build/libs")
        val versionSuffix = if (options.hasVersionSuffixOnJar) "-1.0.0" else ""
        val outJarLibPath =outJarDirPath.resolve("$fixtureName$versionSuffix.jar")
        assertThat(outJarLibPath).exists().isRegularFile

        val outJarSourcesPath = outJarDirPath.resolve("$fixtureName$versionSuffix-sources.jar")
        if (options.createsSourceJar) {
            assertThat(outJarSourcesPath).exists().isRegularFile
        } else {
            assertThat(outJarSourcesPath).doesNotExist()
        }

        val manifestPath = fixtureDir.resolve("build/tmp/jar/MANIFEST.MF")
        val manifest = Manifest(manifestPath.inputStream()).mainAttributes
        assertThat(manifest)
            .containsEntry(Name.CLASS_PATH, options.classpath)
            .containsEntry(Name.IMPLEMENTATION_VENDOR, options.implementationVendor)
            .containsEntry(Name.IMPLEMENTATION_TITLE, fixtureName)
            .containsEntry(Name.IMPLEMENTATION_VERSION, "1.0.0")

        options.additionalChecks?.invoke(fixtureDir)
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
                        createsSourceJar = true,
                        implementationVendor = "Cloudflight XYZ",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-constraints",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true,
                        checkConfigurationInTestOutput = false,
                        classpath = "commons-io-2.8.0.jar"
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default",
                        languageVersion = 11,
                        encoding = "UTF-8",
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
                        createsSourceJar = false,
                        hasVersionSuffixOnJar = false,
                        implementationVendor = "Cloudflight Test Vendor",
                        inferModulePath = true,
                        environment = mapOf(EnvironmentUtils.ENV_DEFAULT_BUILD to true.toString()),
                        additionalChecks = { fixtureDir ->
                            val developmentProperties =
                                fixtureDir.resolve("build/resources/main/development.properties")
                            assertThat(developmentProperties).doesNotExist()
                        }
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-application",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        createsSourceJar = false,
                        implementationVendor = "Cloudflight Test Vendor",
                        inferModulePath = true,
                        additionalChecks = { fixtureDir ->
                            val developmentProperties =
                                fixtureDir.resolve("build/resources/main/development.properties")
                            assertThat(developmentProperties).exists()
                        }
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-junit4",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        createsSourceJar = true,
                        successfulTestCount = 1,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-junit5",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        createsSourceJar = true,
                        successfulTestCount = 1,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-testNG",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        createsSourceJar = true,
                        successfulTestCount = 1,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
            )
        }
    }
}

private val JAVA_FIXTURE_PATH = Paths.get("java")
private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, environment:Map<String, String>,testWork: ProjectFixture.() -> T): T =
    useFixture(JAVA_FIXTURE_PATH, fixtureName, gradleVersion, environment, testWork)