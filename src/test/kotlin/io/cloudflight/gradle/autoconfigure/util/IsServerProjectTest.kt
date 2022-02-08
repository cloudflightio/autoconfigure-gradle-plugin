package io.cloudflight.gradle.autoconfigure.util

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class IsServerProjectTest {

    @Test
    fun `A project without the suffix is not considered a server project`() {
        val project = ProjectBuilder.builder().withName("test").build()
        project.extensions.extraProperties.set(SERVER_PROJECT_SUFFIX, "-suffix")
        assertThat(isServerProject(project)).isFalse
    }

    @Test
    fun `A project with the suffix is considered a server project`() {
        val project = ProjectBuilder.builder().withName("test-suffix").build()
        project.extensions.extraProperties.set(SERVER_PROJECT_SUFFIX, "-suffix")
        assertThat(isServerProject(project)).isTrue
    }

}