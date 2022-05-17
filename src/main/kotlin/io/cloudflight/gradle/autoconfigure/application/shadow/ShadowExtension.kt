package io.cloudflight.gradle.autoconfigure.application.shadow

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin

object ShadowExtension {

    fun create(project: Project) {
        project.plugins.apply(ShadowPlugin::class.java)
        project.plugins.apply(ApplicationPlugin::class.java)
    }

    fun configure(project: Project) {
        val shadowJar = project.tasks.getByName(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME) as ShadowJar
        shadowJar.mergeServiceFiles()
    }

}