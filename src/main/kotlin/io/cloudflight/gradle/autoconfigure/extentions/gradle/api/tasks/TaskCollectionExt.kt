package io.cloudflight.gradle.autoconfigure.extentions.gradle.api.tasks

import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import kotlin.reflect.KClass


/**
 * @see TaskCollection.withType
 */
internal fun <T : Task, S : T> TaskCollection<T>.withType(klass: KClass<S>): NamedDomainObjectSet<S> = this.withType(klass.java)


/**
 * @see TaskCollection.withType
 */
internal fun <T : Task, S : T> TaskCollection<T>.withType(klass: KClass<S>, configuration: (it: S) -> Unit): DomainObjectCollection<S> = this.withType(klass.java, configuration)

/**
 * @see TaskCollection.named
 */
internal fun <T : Task, S : T> TaskCollection<T>.named(name: String, klass: KClass<S>): TaskProvider<S> = this.named(name, klass.java)


/**
 * @see TaskCollection.named
 */
internal fun <T: Task, S : T> TaskCollection<T>.named(name: String, klass: KClass<S>, configuration: (it: S) -> Unit): NamedDomainObjectProvider<S> = this.named(name, klass.java, configuration)
