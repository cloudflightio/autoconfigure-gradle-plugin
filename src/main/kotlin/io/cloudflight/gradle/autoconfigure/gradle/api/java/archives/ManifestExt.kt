package io.cloudflight.gradle.autoconfigure.gradle.api.java.archives

import org.gradle.api.java.archives.Manifest

/**
 * @see Manifest.attributes
 */
internal fun Manifest.attributes(vararg attributes: Pair<String, *>): Manifest {
    this.attributes(mapOf(*attributes))
    return this
}
