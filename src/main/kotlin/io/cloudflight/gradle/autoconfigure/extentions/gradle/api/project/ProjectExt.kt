package io.cloudflight.gradle.autoconfigure.extentions.gradle.api.project

import org.gradle.api.Plugin
import org.gradle.api.Project

fun Project.withPlugin(plugin: Class<out Plugin<*>>, callable: () -> Any) {
    if (plugins.hasPlugin(plugin)) {
        callable.invoke()
    } else {
        plugins.withType(plugin).whenPluginAdded { callable.invoke() }
    }
}