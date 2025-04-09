package io.cloudflight.gradle.autoconfigure.application.development

import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.WriteProperties
import org.gradle.language.jvm.tasks.ProcessResources

object DevelopmentExtension {

    fun create(project: Project) {
        val propertiesTask =
            project.tasks.register("clfDevelopmentProperties", WriteProperties::class.java) {
                it.property("development.name", project.name)
                it.property("development.group", project.group.toString())
                it.property("development.version", project.version.toString())
                it.encoding = "UTF-8"
                it.group = AutoConfigureGradlePlugin.TASK_GROUP
                it.destinationFile.set(project.layout.buildDirectory.file("generated/resources/development/development.properties"))
            }
        project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, ProcessResources::class.java).get()
            .from(propertiesTask.get())
    }
}
