package io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins

import org.gradle.api.plugins.ExtraPropertiesExtension

/**
 * Type save version of get.
 * @see ExtraPropertiesExtension.get
 */
internal inline fun <reified T> ExtraPropertiesExtension.getValue(key: String): T {
    val value = this[key]
    return value as T
}

/**
 * @see ExtraPropertiesExtension.has
 */
internal operator fun ExtraPropertiesExtension.contains(key: String): Boolean = this.has(key)