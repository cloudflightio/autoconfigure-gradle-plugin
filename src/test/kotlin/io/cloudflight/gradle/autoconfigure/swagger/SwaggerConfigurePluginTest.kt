package io.cloudflight.gradle.autoconfigure.swagger

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
import java.nio.file.Paths
import java.util.jar.Attributes.Name
import java.util.jar.Manifest
import java.util.stream.Stream
import kotlin.io.path.inputStream

data class TestOptions(
    val fixtureName: String,
    val gradleVersion: String? = null,
)

class KotlinConfigurePluginTest {


    @ParameterizedTest
    @MethodSource("singleSwaggerModuleArguments")
    fun `the supplied options are used to configure the Swaggerlugin`(
        options: TestOptions
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion) {
        val result = run("clean", "build")


        println(result.output)
    }

    companion object {

        @JvmStatic
        fun singleSwaggerModuleArguments(): Stream<Arguments> {
            // keep in sync with the kotlin version in libs.versions.toml
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-swagger-module",
                    )
                )
            )
        }
    }
}

private val KOTLIN_FIXTURE_PATH = Paths.get("swagger")
private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture(KOTLIN_FIXTURE_PATH, fixtureName, gradleVersion, testWork)