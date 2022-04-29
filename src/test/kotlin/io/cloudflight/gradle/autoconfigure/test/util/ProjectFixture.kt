package io.cloudflight.gradle.autoconfigure.test.util

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.nio.file.Path

private val FIXTURES_BASE_DIR = Path.of("src", "test", "fixtures")

internal class ProjectFixture(fixtureBaseDir: Path, val fixtureName: String, val gradleVersion: String? = null) {

    val fixtureDir: Path = FIXTURES_BASE_DIR.resolve(fixtureBaseDir).resolve(fixtureName)

    fun runCleanBuild(): BuildResult = run("clean", "build")

    fun runBuild(): BuildResult = run("build")

    fun runTasks() = run("tasks")

    fun run(first: String, vararg tasks: String): BuildResult {
        val runner = createRunner(first, *tasks, "--stacktrace", "--info", "--rerun-tasks")
        return runner.build().also {
            // do some checks that should be true for each biuld
            assertThat(it.normalizedOutput).doesNotContain("Execution optimizations have been disabled for task")
        }
    }

    fun createRunner(first: String, vararg tasks: String): GradleRunner {
        var runner = GradleRunner.create()
            .withProjectDir(fixtureDir.toFile())
            .withPluginClasspath()
            .withArguments(first, *tasks)

        if (gradleVersion != null) {
            runner = runner.withGradleVersion(gradleVersion)
        }

        return runner
    }
}

internal fun <T> useFixture(
    fixtureBaseDir: Path,
    fixtureName: String,
    gradleVersion: String?,
    testWork: ProjectFixture.() -> T
): T {
    val fixture = ProjectFixture(fixtureBaseDir, fixtureName, gradleVersion)
    return fixture.testWork()
}
