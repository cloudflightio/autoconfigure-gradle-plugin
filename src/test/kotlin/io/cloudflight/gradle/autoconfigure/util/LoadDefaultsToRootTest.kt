package io.cloudflight.gradle.autoconfigure.util

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LoadDefaultsToRootTest {

    private val resourceUrl = javaClass.getResource("defaults.properties")!!
    private lateinit var defaultProperties: Properties

    @BeforeEach
    fun beforeEach() {
        val defaultResource = resourceUrl.openStream()
        defaultResource.use {
            defaultProperties = Properties()
            defaultProperties.load(it)
        }
    }

    @Test
    fun `loads default values to the root project for single-module projects`() {
        val project = ProjectBuilder.builder().build()

        loadDefaultsToRoot(project, resourceUrl)

        assertThat(project.extensions.extraProperties.properties).containsAllEntriesOf(defaultProperties.withStringKeys)
    }

    @Test
    fun `loads default values to the root project for nested-module projects`() {
        val root = ProjectBuilder.builder().build()
        val subProject = ProjectBuilder.builder().withParent(root).build()
        val subSubProject = ProjectBuilder.builder().withParent(subProject).build()

        loadDefaultsToRoot(subSubProject, resourceUrl)

        assertThat(root.extensions.extraProperties.properties).containsAllEntriesOf(defaultProperties.withStringKeys)
    }

    @Test
    fun `does not override existing values`() {
        val project = ProjectBuilder.builder().build()
        val extraProperties = project.extensions.extraProperties
        extraProperties.set("someProperty", "overridden")

        loadDefaultsToRoot(project, resourceUrl)

        val expectedProperties = mapOf<String, Any>("someProperty" to "overridden", "someOtherProperty" to "someOtherValue")
        assertThat(project.extensions.extraProperties.properties).containsAllEntriesOf(expectedProperties)
    }

    private val Properties.withStringKeys: Map<String, Any>
        get() {
            return this as Map<String, Any>
        }
}