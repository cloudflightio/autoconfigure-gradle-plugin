package io.cloudflight.gradle.autoconfigure.node

import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnTask
import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin
import io.cloudflight.gradle.autoconfigure.util.BuildUtils
import io.cloudflight.gradle.autoconfigure.util.EnvironmentUtils
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal object YarnConfiguration {

    fun apply(project: Project, node: NodeConfigurePluginExtension) {
        val taskClass = YarnTask::class.java
        val taskPrefix = "clfYarn"

        val install = project.tasks.getByName(YarnInstallTask.NAME) as YarnInstallTask

        if (BuildUtils.isIntegrationBuild()) {
            install.args.set(listOf("--immutable", "--check-cache"))
        }

        val updateVersion = project.tasks.register("${taskPrefix}UpdateVersion", taskClass) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.yarnCommand.set(project.provider {
                listOf(
                    "version",
                    project.version.toString()
                )
            })
            t.inputs.files(node.inputFiles)
        }

        val lint = project.tasks.register(NodeConfigurePlugin.YARN_LINT_TASK_NAME, taskClass) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("run", "lint"))
            t.dependsOn(install)
            t.inputs.files(node.inputFiles)
            t.outputs.upToDateWhen { true }
        }

        project.tasks.register("${taskPrefix}BuildDev", taskClass) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("run", "build:dev"))
            t.dependsOn(install)
        }

        val build = project.tasks.register("${taskPrefix}Build", taskClass) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("run", "build"))
            t.dependsOn(install)
            t.inputs.files(node.inputFiles)
            t.outputs.dir(node.destinationDir)
        }

        project.tasks.register("${taskPrefix}Audit", taskClass) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("audit"))
            t.dependsOn(install)
        }

        if (BuildUtils.isIntegrationBuild() && !EnvironmentUtils.isVerifyBuild()) {
            install.dependsOn(updateVersion)
        }

        project.tasks.getByName(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(lint)

        //this prevents running npmBuild each time when a project is started via intellij
        project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).mustRunAfter(build)
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn(build)

        val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        val sourceSetMain = javaPluginExtension.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        sourceSetMain.java.srcDirs(NpmHelper.determineSourceDirs(project))
        sourceSetMain.output.dir(mapOf("builtBy" to build), node.destinationDir)

        if (NpmHelper.hasScript("test", project.file(NpmHelper.PACKAGE_JSON))) {
            val npmTest = project.tasks.register("${taskPrefix}Test", taskClass) { t ->
                t.group = AutoConfigureGradlePlugin.TASK_GROUP
                t.args.set(listOf("run", "test"))
                t.dependsOn(listOf(install, build))
                t.inputs.files(node.inputFiles)
                t.environment.put("GRADLE_BUILD", true.toString())
                t.environment.put("INTEGRATION_BUILD", BuildUtils.isIntegrationBuild().toString())
            }
            project.tasks.getByName("test").dependsOn(npmTest)
        }
    }
}
