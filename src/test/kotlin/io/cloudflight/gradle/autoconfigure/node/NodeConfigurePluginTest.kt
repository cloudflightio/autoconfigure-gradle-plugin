package io.cloudflight.gradle.autoconfigure.node

import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import io.cloudflight.gradle.autoconfigure.util.EnvironmentUtils
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
    val environment: Map<String, String> = emptyMap(),
    val nodeVersion: String = NODE_VERSION,
    val tasksThatShouldHaveRun: Set<String> = emptySet(),
    val assertUpToDateRerun: Boolean = true
)

class NodeConfigurePluginTest {

    @ParameterizedTest
    @MethodSource("singleNodeModuleArguments")
    fun `the supplied options are used to configure the NodePlugin`(
        options: TestOptions
    ): Unit = nodeFixture(options.fixtureName, options.environment) {
        val result = run(LifecycleBasePlugin.CLEAN_TASK_NAME, LifecycleBasePlugin.BUILD_TASK_NAME)

        println(result.output)

        val map = result.tasks.map { it.path.substringAfterLast(":") }
        if (options.tasksThatShouldHaveRun.isNotEmpty()) {
            assertThat(map).containsAnyElementsOf(options.tasksThatShouldHaveRun)
        }

        if (options.assertUpToDateRerun) {
            val result2 = run(LifecycleBasePlugin.BUILD_TASK_NAME, forceRerunTasks = false)
            result2.tasks.forEach {
                assertThat(it.outcome).`as`("${it.path} is up-to-date")
                    .isIn(TaskOutcome.UP_TO_DATE, TaskOutcome.NO_SOURCE)
            }
        }
    }

    companion object {

        @JvmStatic
        fun singleNodeModuleArguments(): Stream<Arguments> {
            // keep in sync with the kotlin version in libs.versions.toml
            return Stream.of(
                arguments(
                    TestOptions(
                        fixtureName = "single-ts-module",
                        assertUpToDateRerun = false // TODO check why this does not work on Github CI
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-ts-module",
                        environment = mapOf(EnvironmentUtils.ENV_DEFAULT_BUILD to true.toString()),
                        tasksThatShouldHaveRun = setOf("clfNpmUpdateVersion"),
                        assertUpToDateRerun = false
                    )
                )
            )
        }
    }
}


private val NODE_FIXTURE_PATH = Paths.get("node")
private fun <T : Any> nodeFixture(
    fixtureName: String,
    environment: Map<String, String>,
    testWork: ProjectFixture.() -> T
): T =
    useFixture(NODE_FIXTURE_PATH, fixtureName, null, environment, testWork)