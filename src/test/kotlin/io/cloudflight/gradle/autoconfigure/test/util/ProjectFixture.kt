package io.cloudflight.gradle.autoconfigure.test.util

import org.assertj.core.api.Assertions.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicLong

internal class ProjectFixture(
    val fixtureDir: Path,
    val fixtureName: String,
    val gradleVersion: String? = null,
    val environment: Map<String, String>? = null
) {

    fun runCleanBuild(): BuildResult = run(LifecycleBasePlugin.CLEAN_TASK_NAME, LifecycleBasePlugin.BUILD_TASK_NAME)

    fun runTasks() = run("tasks")

    fun run(
        first: String,
        vararg tasks: String,
        printStackTrace: Boolean = true,
        forceRerunTasks: Boolean = true,
        infoLoggerEnabled: Boolean = true
    ): BuildResult {
        val arguments = mutableListOf(first)
        arguments.addAll(tasks)
        if (printStackTrace) {
            arguments.add("--stacktrace")
        }
        if (infoLoggerEnabled) {
            arguments.add("--info")
        }
        if (forceRerunTasks) {
            arguments.add("--rerun-tasks")
        }
        val runner = createRunner(arguments)
        return runner.build().also {
            // do some checks that should be true for each build
            assertThat(it.normalizedOutput).doesNotContain("Execution optimizations have been disabled for task")
        }
    }


    fun createRunner(arguments: List<String>): GradleRunner {
        val sysEnv = mutableMapOf<String, String>()
        sysEnv.putAll(System.getenv())

        // we don't wanna pollute our test cases with the System Environment from Github Actions. If wanna simulate
        // a CI build, that should come directly from the TestFixture
        sysEnv.remove("GITHUB_ACTIONS")
        sysEnv.remove("GITHUB_EVENT_NAME")

        environment?.let { sysEnv.putAll(it) }

        var runner = GradleRunner.create()
            .withProjectDir(fixtureDir.toFile())
            .withPluginClasspath()
            .withEnvironment(sysEnv)
            // we need to ensure that we start a new gradle daemon on every run in order to not re-use the class io.cloudflight.ci.info.CI which intializes itself
            // once at startup with the current environment, but if the environment changes (which can't be the case in a real-life scenario) then
            // we don't recognize this any more. And the only - very dirty - way to ensure we're not re-using a daemon turns our to be
            // that one: https://discuss.gradle.org/t/testkit-how-to-turn-off-daemon/17843/6
            .withArguments(arguments + ("-Dorg.gradle.jvmargs=-Xmx" + (256 * 1024 * 1024 + counter.incrementAndGet())))

        if (gradleVersion != null) {
            runner = runner.withGradleVersion(gradleVersion)
        }

        return runner
    }

    fun buildDir(subModuleName: String? = null): Path = this.fixtureDir.resolve(subModuleName ?: "").resolve("build")

    companion object {
        private val counter = AtomicLong(0)
    }
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
    return useFixture(
        FIXTURES_BASE_DIR.resolve(fixtureBaseDir).resolve(fixtureName),
        fixtureName,
        gradleVersion,
        environment,
        testWork
    )
}
