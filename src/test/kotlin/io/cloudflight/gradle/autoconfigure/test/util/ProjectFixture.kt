package io.cloudflight.gradle.autoconfigure.test.util

import org.assertj.core.api.Assertions.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path
import java.nio.file.Paths

internal class ProjectFixture(
    val fixtureDir: Path,
    val fixtureName: String,
    val gradleVersion: String? = null,
    val environment: Map<String, String>? = null
) {

    fun runCleanBuild(): BuildResult = run(LifecycleBasePlugin.CLEAN_TASK_NAME, LifecycleBasePlugin.BUILD_TASK_NAME)

    fun runBuild(): BuildResult = run(LifecycleBasePlugin.BUILD_TASK_NAME)

    fun runTasks() = run("tasks")

    fun run(first: String, vararg tasks: String, printStackTrace: Boolean = true, forceRerunTasks: Boolean = true): BuildResult {
        val arguments = mutableListOf(first)
        arguments.addAll(tasks)
        if (printStackTrace) {
            arguments.add("--stacktrace")
        }
        arguments.add("--info")
        if (forceRerunTasks) {
            arguments.add("--rerun-tasks")
        }
        val runner = createRunner(arguments)
        return runner.build().also {
            // do some checks that should be true for each biuld
            assertThat(it.normalizedOutput).doesNotContain("Execution optimizations have been disabled for task")
        }
    }

    fun createRunner(arguments: List<String>): GradleRunner {
        val sysEnv = mutableMapOf<String, String>()
        sysEnv.putAll(System.getenv())
        environment?.let { sysEnv.putAll(it) }
        var runner = GradleRunner.create()
            .withProjectDir(fixtureDir.toFile())
            .withPluginClasspath()
            .withEnvironment(sysEnv)
            .withArguments(arguments)

        if (gradleVersion != null) {
            runner = runner.withGradleVersion(gradleVersion)
        }

        return runner
    }

    fun buildDir(subModuleName: String? = null): Path = this.fixtureDir.resolve(subModuleName ?: "").resolve("build")
}


internal fun <T> useFixture(
    fixtureBaseDir: Path,
    fixtureName: String,
    gradleVersion: String?,
    environment: Map<String, String>? = emptyMap(),
    testWork: ProjectFixture.() -> T
): T {
    val fixture = ProjectFixture(fixtureBaseDir, fixtureName, gradleVersion, environment)
    return fixture.testWork()
}

private val FIXTURES_BASE_DIR = Paths.get("src", "test", "fixtures")

/**
 * @param fixtureBaseDir the name of the directory inside `src/test/fixtures`
 */
internal fun <T> useFixture(
    fixtureBaseDir: String,
    fixtureName: String,
    gradleVersion: String?,
    environment: Map<String, String>? = emptyMap(),
    testWork: ProjectFixture.() -> T
): T {
    return useFixture(FIXTURES_BASE_DIR.resolve(fixtureBaseDir).resolve(fixtureName), fixtureName, gradleVersion, environment, testWork)
}
