package io.cloudflight.gradle.autoconfigure.util

import io.cloudflight.ci.info.CI

object EnvironmentUtils {
    fun isDefaultBuild(): Boolean {
        return getBoolean(ENV_DEFAULT_BUILD) || (CI.isCI && CI.isPR != false)
    }

    fun isVerifyBuild(): Boolean {
        return getBoolean(ENV_VERIFY_BUILD) || CI.isPR == true
    }

    fun isPublishBuild(): Boolean {
        return getBoolean(ENV_PUBLISH_BUILD)
    }

    private fun getBoolean(name: String): Boolean {
        return System.getenv(name).toBoolean()
    }

    const val ENV_DEFAULT_BUILD = "DEFAULT_BUILD"
    const val ENV_VERIFY_BUILD = "VERIFY_BUILD"
    const val ENV_PUBLISH_BUILD = "PUBLISH_BUILD"
}