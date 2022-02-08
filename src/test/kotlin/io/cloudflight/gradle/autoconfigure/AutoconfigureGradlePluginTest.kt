package io.cloudflight.gradle.autoconfigure

import io.cloudflight.gradle.autoconfigure.java.JavaAutoconfigurePlugin
import io.cloudflight.gradle.autoconfigure.test.util.ProjectFixture
import io.cloudflight.gradle.autoconfigure.test.util.normalizedOutput
import io.cloudflight.gradle.autoconfigure.test.util.useFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

class AutoconfigureGradlePluginTest {

    @Test
    fun `JavaAutoconfigurePlugin is not applied to a non java project`(): Unit = autoconfigureFixture("no-autoconfig") {
        val result = runTasks()

        assertThat(result.normalizedOutput).doesNotContain(JavaAutoconfigurePlugin::class.qualifiedName)
    }

    @Test
    fun `JavaAutoconfigurePlugin is applied to a single-module java project`(): Unit = autoconfigureFixture("single-java-module") {
        val result = runTasks()

        assertThat(result.normalizedOutput).contains(JavaAutoconfigurePlugin::class.qualifiedName)
    }

    @Test
    fun `in a multi module project the JavaAutoconfigurePlugin is only applied to java project`(): Unit = autoconfigureFixture("multi-module") {
        val result = runTasks()

        val startIndexNoPluginApplied = result.normalizedOutput.indexOf("Project no-plugin-applied start")
        val endIndexNoPluginApplied = result.normalizedOutput.indexOf("Project no-plugin-applied stop", startIndexNoPluginApplied)
        val noPluginAppliedOutput = result.normalizedOutput.substring(startIndexNoPluginApplied, endIndexNoPluginApplied)

        val startIndexJavaModule = result.normalizedOutput.indexOf("Project java-module start")
        val endIndexJavaModule = result.normalizedOutput.indexOf("Project java-module stop", startIndexJavaModule)
        val javaModuleOutput = result.normalizedOutput.substring(startIndexJavaModule, endIndexJavaModule)

        assertThat(noPluginAppliedOutput).doesNotContain(JavaAutoconfigurePlugin::class.qualifiedName)
        assertThat(javaModuleOutput).contains(JavaAutoconfigurePlugin::class.qualifiedName)
    }

}

private val AUTOCONFIGURE_FIXTURE_PATH = Path.of("autoconfigure")
private fun <T : Any> autoconfigureFixture(fixtureName: String, gradleVersion: String? = null, testWork: ProjectFixture.() -> T): T =
    useFixture(AUTOCONFIGURE_FIXTURE_PATH, fixtureName, gradleVersion, testWork)
