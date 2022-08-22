package io.cloudflight.gradle.autoconfigure.samples

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

data class TestOptions(
    val fixtureName: String,
)

/**
 * Runs a `clean build` on all projects inside the folder `samples`
 */
class SamplesPluginTest {

    @ParameterizedTest
    @MethodSource("sampleProjectDirectories")
    fun `the sample application is built correctly`(
        options: TestOptions
    ): Unit = swaggerFixture(options.fixtureName) {
        run(LifecycleBasePlugin.CLEAN_TASK_NAME, LifecycleBasePlugin.BUILD_TASK_NAME)
    }

    companion object {

        @JvmStatic
        fun sampleProjectDirectories(): Stream<Arguments> {
            return SAMPLES_FIXTURE_PATH
                .toFile()
                .listFiles()!!
                .filter { it.isDirectory }
                .map { arguments(TestOptions(fixtureName = it.name)) }.stream()
        }
    }
}

private val SAMPLES_FIXTURE_PATH = Paths.get("samples")


private fun <T : Any> swaggerFixture(fixtureName: String, testWork: ProjectFixture.() -> T): T =
    useFixture(SAMPLES_FIXTURE_PATH.resolve(fixtureName), fixtureName, null, emptyMap(), testWork)