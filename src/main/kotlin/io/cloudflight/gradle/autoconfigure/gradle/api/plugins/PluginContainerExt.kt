package io.cloudflight.gradle.autoconfigure.gradle.api.plugins

import org.gradle.api.Plugin
import org.gradle.api.plugins.PluginContainer
import kotlin.reflect.KClass

/**
 * @see PluginContainer.apply
 */
internal fun <T : Plugin<*>> PluginContainer.apply(klass: KClass<T>): T = this.apply(klass.java)
