package io.cloudflight.gradle.autoconfigure.kotlin

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.normalizedOutput
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowingConsumer
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
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
    val hasVersionSuffixOnJar: Boolean = true,
    val implementationVendor: String,
    val inferModulePath: Boolean,
    val gradleVersion: String? = null,
    val kotlinVersion: String
)

class KotlinConfigurePluginTest {


    @ParameterizedTest
    @MethodSource("singleKotlinModuleArguments")
    fun `the supplied options are used to configure the KotlinPlugin`(
        options: TestOptions
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion) {
        val result = run("clean", "build", "dependencies", "--configuration", "runtimeClasspath")

        val outJarDirPath = buildDir().resolve("libs")
        val versionSuffix = if (options.hasVersionSuffixOnJar) "-1.0.0" else ""
        val outJarLibPath = outJarDirPath.resolve("$fixtureName$versionSuffix.jar")
        assertThat(outJarLibPath).exists().isRegularFile

        assertThat(outJarLibPath).exists().isRegularFile

        val outJarSourcesPath = outJarDirPath.resolve("$fixtureName$versionSuffix-sources.jar")
        if (options.createsSourceJar) {
            assertThat(outJarSourcesPath).exists().isRegularFile
        } else {
            assertThat(outJarSourcesPath).doesNotExist()
        }

        val manifestPath = buildDir().resolve("tmp/jar/MANIFEST.MF")
        val manifest = Manifest(manifestPath.inputStream()).mainAttributes
        assertThat(manifest)
            .containsEntry(Name.IMPLEMENTATION_VENDOR, options.implementationVendor)
            .containsEntry(Name.IMPLEMENTATION_TITLE, fixtureName)
            .containsEntry(Name.IMPLEMENTATION_VERSION, "1.0.0")

        assertThat(result.normalizedOutput).contains("--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:${options.kotlinVersion}\n")

        // validate that Kotlin uses the jdk configured by the java toolchain
        val jdkHome = result.extractJavaToolchainJdkHome()
        val kotlinJdkLines = result.normalizedOutput.lines().filter { it.startsWith("[KOTLIN]") }
        val validateJdkHome = ThrowingConsumer<String> { input -> assertThat(input).contains(jdkHome) }
        assertThat(kotlinJdkLines).isNotEmpty.allSatisfy(validateJdkHome)

        println(result.output)
    }

    companion object {

        @JvmStatic
        fun singleKotlinModuleArguments(): Stream<Arguments> {
            // keep in sync with the kotlin version in libs.versions.toml
            val currentKotlinVersion = "1.8.21"
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
                        fixtureName = "single-kotlin-module-constraints",
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
                        fixtureName = "single-kotlin-module-kapt",
                        languageVersion = 11,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit5 as test platform",
                        createsSourceJar = true,
                        implementationVendor = "Cloudflight",
                        inferModulePath = true,
                        kotlinVersion = currentKotlinVersion
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-kotlin-module-server",
                        languageVersion = 8,
                        encoding = "UTF-8",
                        testPlatformMessage = "Enabled Junit5 as test platform",
                        createsSourceJar = false,
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

private fun BuildResult.extractJavaToolchainJdkHome(): String {
    val prefix = "javaToolchain.jdkHome: "
    return this.normalizedOutput.lines().first { it.startsWith(prefix) }.removePrefix(prefix)
}

private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture("kotlin", fixtureName, gradleVersion, emptyMap(), testWork)