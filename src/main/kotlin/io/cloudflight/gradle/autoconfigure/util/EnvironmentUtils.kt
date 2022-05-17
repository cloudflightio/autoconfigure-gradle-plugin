package io.cloudflight.gradle.autoconfigure.util

object EnvironmentUtils {
    fun isDefaultBuild(): Boolean {
        return getBoolean(ENV_DEFAULT_BUILD)
    }

    fun isVerifyBuild(): Boolean {
        return getBoolean(ENV_VERIFY_BUILD)
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