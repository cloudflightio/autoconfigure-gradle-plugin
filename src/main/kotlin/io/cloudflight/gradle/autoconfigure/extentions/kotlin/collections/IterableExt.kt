package io.cloudflight.gradle.autoconfigure.extentions.kotlin.collections

/**
 * Returns true if an element in the collection satisfies the predicate.
 */
internal fun <T> Iterable<T>.contains(predicate: (T) -> Boolean): Boolean {
    val value = this.find(predicate)
    return value != null
}