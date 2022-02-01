package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.normalizedOutput
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.JavaVersion
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.jar.Attributes.Name
import java.util.jar.Manifest
import java.util.stream.Stream
import kotlin.io.path.inputStream

data class TestOptions(
    val fixtureName: String,
    val javaVersion: String,
    val encoding: String,
    val createsSourceJar: Boolean,
    val implementationVendor: String,
    val inferModulePath: Boolean,
    val gradleVersion: String? = null,
)

class JavaConfigurePluginTest {

    @ParameterizedTest
    @MethodSource("singleJavaModuleArguments")
    fun `the supplied options are used to configure the JavaPlugin`(
        options: TestOptions
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion) {
        val result = runCleanBuild()
        assertThat(result.normalizedOutput).contains(
            """
                > Task :compileJava
                javaPluginExtension.modularity.inferModulePath: ${options.inferModulePath}
                javaPluginExtension.sourceCompatibility: ${options.javaVersion}
                javaPluginExtension.targetCompatibility: ${options.javaVersion}
                compileJava.options.encoding: ${options.encoding}
            """.trimIndent()
        ).contains(
            """
                > Task :compileTestJava
                compileTestJava.options.encoding: ${options.encoding}
            """.trimIndent()
        )

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
        assertThat(manifest).containsEntry(
            Name("Created-By"),
            "$systemJavaVersion (${System.getProperty("java.vendor")})"
        )
            .containsEntry(Name.CLASS_PATH, "")
            .containsEntry(Name.IMPLEMENTATION_VENDOR, options.implementationVendor)
            .containsEntry(Name.IMPLEMENTATION_TITLE, fixtureName)
            .containsEntry(Name.IMPLEMENTATION_VERSION, "1.0.0")
    }

    companion object {
        val systemJavaVersion: String = System.getProperty("java.version")

        @JvmStatic
        fun singleJavaModuleArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-library",
                        javaVersion = "11",
                        encoding = "UTF-8",
                        createsSourceJar = true,
                        implementationVendor = "Cloudflight Test Vendor",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default",
                        javaVersion = JavaVersion.toVersion(systemJavaVersion).toString(),
                        encoding = "UTF-8",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-application",
                        javaVersion = "11",
                        encoding = "UTF-8",
                        createsSourceJar = false,
                        implementationVendor = "Cloudflight Test Vendor",
                        inferModulePath = true
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-java-module-default",
                        javaVersion = JavaVersion.toVersion(systemJavaVersion).toString(),
                        encoding = "UTF-8",
                        createsSourceJar = true,
                        implementationVendor = "",
                        inferModulePath = false,
                        gradleVersion = "6.3"
                    )
                ),
            )
        }
    }

}

private val JAVA_FIXTURE_PATH = Path.of("java")
private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture(JAVA_FIXTURE_PATH, fixtureName, gradleVersion, testWork)