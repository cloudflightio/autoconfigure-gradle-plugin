package io.cloudflight.gradle.autoconfigure.swagger

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

data class TestOptions(
    val fixtureName: String,
    val swaggerFileName: String = fixtureName,
    val gradleVersion: String? = null,
)

class KotlinConfigurePluginTest {


    @ParameterizedTest
    @MethodSource("singleSwaggerModuleArguments")
    fun `the supplied options are used to configure the Swaggerlugin`(
        options: TestOptions
    ): Unit = javaFixture(options.fixtureName, options.gradleVersion) {
        val result = run("clean", "build")

        assertThat(fixtureDir.resolve("build/generated/resources/openapi/${options.swaggerFileName}.json")).exists()
        assertThat(fixtureDir.resolve("build/generated/resources/openapi/${options.swaggerFileName}.yaml")).exists()
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
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-swagger-module-override",
                        swaggerFileName = "myswagger"
                    )
                )
            )
        }
    }
}

private val KOTLIN_FIXTURE_PATH = Paths.get("swagger")
private fun <T : Any> javaFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture(KOTLIN_FIXTURE_PATH, fixtureName, gradleVersion, emptyMap(), testWork)