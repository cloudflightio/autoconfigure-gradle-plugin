package io.cloudflight.gradle.autoconfigure.skeleton

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
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
    @MethodSource("skeletonArguments")
    fun `the supplied options are used to configure the Swaggerplugin`(
        options: TestOptions
    ): Unit = swaggerFixture(options.fixtureName) {
        val result = run("clean", "build")

    }

    companion object {

        @JvmStatic
        fun skeletonArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "kotlin-springboot-angular",
                    )
                )
            )
        }
    }
}

private val SKELETON_FIXTURE_PATH = Paths.get("skeletons")
private fun <T : Any> swaggerFixture(fixtureName: String, testWork: ProjectFixture.() -> T): T =
    useFixture(SKELETON_FIXTURE_PATH, fixtureName, null, emptyMap(), testWork)