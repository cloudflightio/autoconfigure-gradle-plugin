package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.contains
import org.gradle.api.Project
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

private val LOG = LoggerFactory.getLogger("io.cloudflight.gradle.autoconfigure.util.loadDefaults")

internal fun loadDefaults(project: Project, defaultsUrl: URL) {
    val defaults = Properties()
    defaultsUrl.openStream().use {
        defaults.load(it)
    }

    val extraProperties = project.extensions.extraProperties
    LOG.debug("properties:\n{}", defaults)

    for (entry in defaults) {
        val key = entry.key as String
        val value = entry.value as String
        LOG.debug("entry: {}, {}", entry, key in extraProperties)

        if (key !in extraProperties) {
            LOG.debug("setting global default for $key: $value")
            extraProperties.set(key, value)
        }
    }
}