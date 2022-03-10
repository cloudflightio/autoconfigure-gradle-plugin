package io.cloudflight.gradle.autoconfigure.java

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.JavaVersion
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class JavaAutoconfigurePluginTest {

    private lateinit var defaultProperties: Properties

    @BeforeEach
    fun beforeEach() {
        val defaultResource = javaClass.getResourceAsStream("/java/defaults.properties")!!
        defaultResource.use {
            defaultProperties = Properties()
            defaultProperties.load(it)
        }
    }

    @Test
    fun `normal project is configured correctly`() {
        val project = ProjectBuilder.builder().withName("test-project").build()
        project.plugins.apply(JavaAutoconfigurePlugin::class)

        val javaConfigurePluginExtension = project.extensions.getByType(JavaConfigurePluginExtension::class)
        val expectedJavaVersion = JavaVersion.toVersion(defaultProperties[JavaAutoconfigurePlugin.JAVA_VERSION]!!)

        assertThat(javaConfigurePluginExtension.applicationBuild.get()).isFalse
        assertThat(javaConfigurePluginExtension.javaVersion.get()).isEqualTo(expectedJavaVersion)
        assertThat(javaConfigurePluginExtension.encoding.get()).isEqualTo(defaultProperties[JavaAutoconfigurePlugin.ENCODING])
    }

    @Test
    fun `server project is configured correctly`() {
        val project = ProjectBuilder.builder().withName("test-project-server").build()
        project.plugins.apply(JavaAutoconfigurePlugin::class)

        val javaConfigurePluginExtension = project.extensions.getByType(JavaConfigurePluginExtension::class)
        val expectedJavaVersion = JavaVersion.toVersion(defaultProperties[JavaAutoconfigurePlugin.JAVA_VERSION]!!)

        assertThat(javaConfigurePluginExtension.applicationBuild.get()).isTrue
        assertThat(javaConfigurePluginExtension.javaVersion.get()).isEqualTo(expectedJavaVersion)
        assertThat(javaConfigurePluginExtension.encoding.get()).isEqualTo(defaultProperties[JavaAutoconfigurePlugin.ENCODING])
    }

}