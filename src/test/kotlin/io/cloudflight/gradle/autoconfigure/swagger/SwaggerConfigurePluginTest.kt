package io.cloudflight.gradle.autoconfigure.swagger

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

data class TestOptions(
    val fixtureName: String,
    val swaggerFileName: String = fixtureName
)

class SwaggerConfigurePluginTest {


    @ParameterizedTest
    @MethodSource("singleSwaggerModuleArguments")
    fun `the supplied options are used to configure the Swaggerplugin`(
        options: TestOptions
    ): Unit = swaggerFixture(options.fixtureName) {
        val result = runCleanBuild()

        assertThat(buildDir().resolve("generated/resources/openapi/${options.swaggerFileName}.json")).exists()
        assertThat(buildDir().resolve("generated/resources/openapi/${options.swaggerFileName}.yaml")).exists()
    }

    @Test
    fun `create client from file`(): Unit = swaggerFixture("generate-swagger-from-file") {
        val result = runCleanBuild()

        assertThat(buildDir("petstore-client").resolve("swagger-code-petstore-api/src/main/java/io/cloudflight/petstore/client/ApiClient.java")).exists()
    }

    companion object {

        @JvmStatic
        fun singleSwaggerModuleArguments(): Stream<Arguments> {
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

private fun <T : Any> swaggerFixture(fixtureName: String, testWork: ProjectFixture.() -> T): T =
    useFixture("swagger", fixtureName, null, emptyMap(), testWork)