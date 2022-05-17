package io.cloudflight.gradle.autoconfigure.util

object BuildUtils {

    fun isIntegrationBuild(): Boolean {
        return EnvironmentUtils.isDefaultBuild() || EnvironmentUtils.isVerifyBuild() || EnvironmentUtils.isPublishBuild()
    }

}