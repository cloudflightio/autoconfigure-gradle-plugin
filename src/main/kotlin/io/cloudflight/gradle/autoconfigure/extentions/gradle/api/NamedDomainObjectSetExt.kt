package io.cloudflight.gradle.autoconfigure.extentions.gradle.api

import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.NamedDomainObjectSet
import kotlin.reflect.KClass

internal fun <T : Any, S : T> NamedDomainObjectSet<T>.withType(klass: KClass<S>): NamedDomainObjectSet<S> = this.withType(klass.java)

internal fun <T : Any, S : T> NamedDomainObjectSet<T>.withType(klass: KClass<S>, configuration: (it: S) -> Unit): DomainObjectCollection<S> = this.withType(klass.java, configuration)

internal fun <T: Any, S : T> NamedDomainObjectSet<T>.named(name: String, klass: KClass<S>): NamedDomainObjectProvider<S> = this.named(name, klass.java)

internal fun <T: Any, S : T> NamedDomainObjectSet<T>.named(name: String, klass: KClass<S>, configuration: (it: S) -> Unit): NamedDomainObjectProvider<S> = this.named(name, klass.java, configuration)
