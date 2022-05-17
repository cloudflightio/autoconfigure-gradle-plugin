package io.cloudflight.gradle.autoconfigure.application.springboot

import org.gradle.api.Project
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

object SpringBootExtension {
    fun create(project: Project) {
        project.plugins.apply(SpringBootPlugin::class.java)
    }

    fun configure(project: Project) {
        val boot = project.tasks.getByName(SpringBootPlugin.BOOT_JAR_TASK_NAME) as BootJar
        boot.archiveBaseName.set(project.name)
        boot.archiveFileName.set("${project.name}.jar")

        val springBoot = project.extensions.getByType(SpringBootExtension::class.java)
        springBoot.buildInfo()
    }
}