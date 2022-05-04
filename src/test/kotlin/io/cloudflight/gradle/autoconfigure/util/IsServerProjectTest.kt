package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.gradle.autoconfigure.AutoConfigureExtension
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class IsServerProjectTest {

    @Test
    fun `A project without the suffix is not considered a server project`() {
        val project = ProjectBuilder.builder().withName("test").build()
        val autoConfigure = project.rootProject.extensions.create("autoConfigure", AutoConfigureExtension::class.java)
        autoConfigure.java.serverProjectSuffix.convention("-suffix")
        assertThat(autoConfigure.java.isServerProject(project).get()).isFalse
    }

    @Test
    fun `A project with the suffix is considered a server project`() {
        val project = ProjectBuilder.builder().withName("test-suffix").build()
        val autoConfigure = project.rootProject.extensions.create("autoConfigure", AutoConfigureExtension::class.java)
        autoConfigure.java.serverProjectSuffix.convention("-suffix")
        assertThat(autoConfigure.java.isServerProject(project).get()).isTrue
    }

}