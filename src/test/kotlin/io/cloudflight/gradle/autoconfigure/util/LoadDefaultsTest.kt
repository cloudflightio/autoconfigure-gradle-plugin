package io.cloudflight.gradle.autoconfigure.util

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class LoadDefaultsTest {

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
    fun `loads default values to the current project`() {
        val project = ProjectBuilder.builder().build()

        loadDefaults(project, resourceUrl)

        assertThat(project.extensions.extraProperties.properties).containsAllEntriesOf(defaultProperties.withStringKeys)
    }

    @Test
    fun `does not override existing values`() {
        val project = ProjectBuilder.builder().build()
        val extraProperties = project.extensions.extraProperties
        extraProperties.set("someProperty", "overridden")

        loadDefaults(project, resourceUrl)

        val expectedProperties = mapOf<String, Any>("someProperty" to "overridden", "someOtherProperty" to "someOtherValue")
        assertThat(project.extensions.extraProperties.properties).containsAllEntriesOf(expectedProperties)
    }

    private val Properties.withStringKeys: Map<String, Any>
        get() {
            return this as Map<String, Any>
        }
}