package io.cloudflight.gradle.autoconfigure.extentions.gradle.api

import org.gradle.api.NamedDomainObjectSet
import kotlin.reflect.KClass

internal fun <T : Any, S : T> NamedDomainObjectSet<T>.withType(klass: KClass<S>): NamedDomainObjectSet<S> = this.withType(klass.java)