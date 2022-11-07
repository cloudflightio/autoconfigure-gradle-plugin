package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.ci.info.CI

object BuildUtils {

    fun isIntegrationBuild(): Boolean {
        return CI.isCI
    }

}