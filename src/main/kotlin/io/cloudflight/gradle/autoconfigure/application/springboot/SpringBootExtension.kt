package io.cloudflight.gradle.autoconfigure.application.springboot

import io.cloudflight.gradle.autoconfigure.java.PopulateManifestAction
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
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
        boot.doFirst(PopulateManifestAction)

        val springBoot = project.extensions.getByType(org.springframework.boot.gradle.dsl.SpringBootExtension::class.java)
        springBoot.buildInfo()

        // we don't need the plain archive, see https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#packaging-executable.and-plain-archives
        project.tasks.named(JavaPlugin.JAR_TASK_NAME) {
            it.enabled = false
        }
    }
}