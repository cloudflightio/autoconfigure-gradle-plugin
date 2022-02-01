package io.cloudflight.gradle.autoconfigure.gradle.api.plugins

import org.gradle.api.plugins.ExtensionContainer
import kotlin.reflect.KClass

/**
 * @see ExtensionContainer.getByType
 */
internal fun <T : Any> ExtensionContainer.getByType(klass: KClass<T>): T = this.getByType(klass.java)

/**
 * @see ExtensionContainer.create
 */
internal fun <T: Any> ExtensionContainer.create(name: String, klass: KClass<T>): T = this.create(name, klass.java)