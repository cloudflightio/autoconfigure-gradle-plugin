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

        val outJarDirPath = buildDir().resolve("libs")
        val outJarSourcesPath = outJarDirPath.resolve("$fixtureName-1.0.0-sources.jar")
        assertThat(outJarSourcesPath).doesNotExist()

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
                        environment = mapOf("GITHUB_ACTIONS" to true.toString()),
                        tasksThatShouldHaveRun = setOf("clfNpmUpdateVersion"),
                        assertUpToDateRerun = false
                    )
                ),
                arguments(
                    TestOptions(
                        fixtureName = "single-ts-module",
                        tasksThatShouldHaveRun = setOf("clfNpmClean"),
                        assertUpToDateRerun = false
                    )
                )
            )
        }
    }
}


private fun <T : Any> nodeFixture(
    fixtureName: String,
    environment: Map<String, String>,
    testWork: ProjectFixture.() -> T
): T =
    useFixture("node", fixtureName, null, environment, testWork)