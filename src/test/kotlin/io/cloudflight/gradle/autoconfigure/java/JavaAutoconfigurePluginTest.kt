package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.AutoConfigureExtension
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.JavaVersion
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class JavaAutoconfigurePluginTest {

    @Test
    fun `normal project is configured correctly`() {
        val project = ProjectBuilder.builder().withName("test-project").build()
        project.plugins.apply(JavaConfigurePlugin::class)

        val javaConfigurePluginExtension = project.extensions.getByType(JavaConfigurePluginExtension::class)
        val autoConfigure = project.rootProject.extensions.getByType(AutoConfigureExtension::class)
        val expectedJavaVersion = autoConfigure.java.javaVersion.get()

        assertThat(javaConfigurePluginExtension.applicationBuild.get()).isFalse
        assertThat(javaConfigurePluginExtension.javaVersion.get()).isEqualTo(expectedJavaVersion)
        assertThat(javaConfigurePluginExtension.encoding.get()).isEqualTo(autoConfigure.java.encoding.get())
    }

    @Test
    fun `server project is configured correctly`() {
        val project = ProjectBuilder.builder().withName("test-project-server").build()
        project.plugins.apply(JavaConfigurePlugin::class)

        val javaConfigurePluginExtension = project.extensions.getByType(JavaConfigurePluginExtension::class)
        val autoConfigure = project.rootProject.extensions.getByType(AutoConfigureExtension::class)
        val expectedJavaVersion = autoConfigure.java.javaVersion.get()

        assertThat(javaConfigurePluginExtension.applicationBuild.get()).isTrue
        assertThat(javaConfigurePluginExtension.javaVersion.get()).isEqualTo(expectedJavaVersion)
        assertThat(javaConfigurePluginExtension.encoding.get()).isEqualTo(autoConfigure.java.encoding.get())
    }

}