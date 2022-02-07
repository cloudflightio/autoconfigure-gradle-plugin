package io.cloudflight.gradle.autoconfigure.extentions.gradle.api.tasks

import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import kotlin.reflect.KClass

/**
 * @see TaskCollection.named
 */
internal fun <T : Task, S : T> TaskCollection<T>.named(name: String, klass: KClass<S>): TaskProvider<S> = this.named(name, klass.java)