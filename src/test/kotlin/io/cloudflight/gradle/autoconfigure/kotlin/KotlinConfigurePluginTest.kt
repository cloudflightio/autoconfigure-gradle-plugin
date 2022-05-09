package io.cloudflight.gradle.autoconfigure.kotlin

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
    val kotlinVersion: String
)

class KotlinConfigurePluginTest {

    private fun Int.toJavaVersion(): String {
        return if (this == 8) {
            "1.8"
        } else {
            this.toString()
        }
    }

    @ParameterizedTest
    @MethodSource("singleKotlinModuleArguments")
    fun `the supplied options are used to configure the KotlinPlugin`(
        options: TestOptions
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion) {
        val result = run("clean", "build", "dependencies", "--configuration", "runtimeClasspath")

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
            .containsEntry(Name.IMPLEMENTATION_VENDOR, options.implementationVendor)
            .containsEntry(Name.IMPLEMENTATION_TITLE, fixtureName)
            .containsEntry(Name.IMPLEMENTATION_VERSION, "1.0.0")

        assertThat(result.normalizedOutput).contains("--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:${options.kotlinVersion}")

        println(result.output)
    }

    companion object {

        @JvmStatic
        fun singleKotlinModuleArguments(): Stream<Arguments> {
            // keep in sync with the kotlin version in libs.versions.toml
            val currentKotlinVersion = "1.6.21"
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-kotlin-module",
                        languageVersion = 8,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit5 as test platform",
                        createsSourceJar = true,
                        implementationVendor = "Cloudflight XYZ",
                        inferModulePath = true,
                        kotlinVersion = currentKotlinVersion
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-kotlin-module-override-kotlinversion",
                        languageVersion = 8,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit5 as test platform",
                        createsSourceJar = true,
                        implementationVendor = "Cloudflight XYZ",
                        inferModulePath = true,
                        kotlinVersion = "1.5.20"
                    )
                )
            )
        }
    }
}

private val KOTLIN_FIXTURE_PATH = Paths.get("kotlin")
private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture(KOTLIN_FIXTURE_PATH, fixtureName, gradleVersion, testWork)