package io.cloudflight.gradle.autoconfigure.node

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

data class TestOptions(
    val fixtureName: String,
    val gradleVersion: String? = null,
    val nodeVersion: String = NODE_VERSION
)

class NodeConfigurePluginTest {

    @ParameterizedTest
    @MethodSource("singleNodeModuleArguments")
    fun `the supplied options are used to configure the NodePlugin`(
        options: TestOptions
    ): Unit = nodeFixture(options.fixtureName, options.gradleVersion) {
        val result = run(LifecycleBasePlugin.CLEAN_TASK_NAME, LifecycleBasePlugin.BUILD_TASK_NAME)

        println(result.output)

        /* TODO check why this does not work on github CI
        val result2 = run(LifecycleBasePlugin.BUILD_TASK_NAME, forceRerunTasks = false)
        result2.tasks.forEach {
            assertThat(it.outcome).`as`("${it.path} is up-to-date").isIn(TaskOutcome.UP_TO_DATE, TaskOutcome.NO_SOURCE)
        }*/
    }

    companion object {

        @JvmStatic
        fun singleNodeModuleArguments(): Stream<Arguments> {
            // keep in sync with the kotlin version in libs.versions.toml
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-ts-module"
                    )
                )
            )
        }
    }
}


private val NODE_FIXTURE_PATH = Paths.get("node")
private fun <T : Any> nodeFixture(fixtureName: String, gradleVersion: String?, testWork: ProjectFixture.() -> T): T =
    useFixture(NODE_FIXTURE_PATH, fixtureName, gradleVersion, emptyMap(), testWork)