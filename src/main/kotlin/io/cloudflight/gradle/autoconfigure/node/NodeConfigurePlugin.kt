package io.cloudflight.gradle.autoconfigure.node

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodeExtension.Companion.DEFAULT_NPM_VERSION
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.util.BuildUtils.isIntegrationBuild
import io.cloudflight.gradle.autoconfigure.util.EnvironmentUtils.isVerifyBuild
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File

internal const val NPM_BUILD_TASK_NAME = "clfNpmBuild"
internal const val NPM_LINT_TASK_NAME = "clfNpmLint"

class NodeConfigurePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(NodePlugin::class)
        project.plugins.apply(JavaConfigurePlugin::class)

        val node = NodeExtension[project]

        // on CI environments we want to use npm ci instead of npm install
        if (isIntegrationBuild()) {
            node.npmInstallCommand.set("ci")
        }

        val nodeExtension = project.extensions.create(EXTENSION_NAME, NodeConfigurePluginExtension::class).apply {
            nodeVersion.convention(NODE_VERSION)
            downloadNode.convention(true)
            npm.npmVersion.convention(DEFAULT_NPM_VERSION)
            npm.destinationDir.convention(File(project.buildDir, "/generated-resources/"))
            npm.inputFiles.convention(
                NpmHelper.determineSourceDirs(project).map { project.fileTree(it) } +
                        listOf(
                            project.file(NpmHelper.ANGULAR_JSON),
                            project.file(NpmHelper.PACKAGE_JSON),
                            project.file("tsconfig.json"),
                            project.file("eslintrc.json")
                        )
            )
        }


        val dummy = NodeExtension(project.rootProject)
        // Ensure node is only downloaded once for all subprojects
        node.workDir.set(dummy.workDir)
        node.npmWorkDir.set(dummy.npmWorkDir)
        node.yarnWorkDir.set(dummy.yarnWorkDir)

        node.download.set(nodeExtension.downloadNode)

        node.version.set(nodeExtension.nodeVersion)
        node.npmVersion.set(nodeExtension.npm.npmVersion)

        val install = project.tasks.getByName(NpmInstallTask.NAME) as NpmInstallTask

        val lint = project.tasks.create(NPM_LINT_TASK_NAME, NpmTask::class.java) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("run", "lint"))
            t.dependsOn(install)
            t.inputs.files(nodeExtension.npm.inputFiles)
            t.outputs.upToDateWhen { true }
        }

        project.tasks.create("clfNpmBuildDev", NpmTask::class.java) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("run", "build:dev"))
            t.dependsOn(install)
        }

        val updateVersion = project.tasks.create("clfNpmUpdateVersion", NpmTask::class.java) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.npmCommand.set(listOf("version"))
            t.args.set(project.provider {
                listOf(
                    project.version.toString(),
                    "--allow-same-version",
                    "--no-git-tag-version"
                )
            })
            t.inputs.files(nodeExtension.npm.inputFiles)
        }

        val build = project.tasks.create(NPM_BUILD_TASK_NAME, NpmTask::class.java) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("run", "build"))
            t.dependsOn(install)
            t.inputs.files(nodeExtension.npm.inputFiles)
            t.outputs.dir(nodeExtension.npm.destinationDir)
        }

        project.tasks.create("clfNpmAudit", NpmTask::class.java) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("audit"))
            t.dependsOn(install)
        }

        val packgeJsonFile = project.file(NpmHelper.PACKAGE_JSON)

        if (isIntegrationBuild() && !isVerifyBuild() && packgeJsonFile.exists()) {
            install.dependsOn(updateVersion)
        }

        project.tasks.getByName(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(lint)

        //this prevents running npmBuild each time when a project is started via intellij
        project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).mustRunAfter(build)
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn(build)

        if (NpmHelper.hasScript("test", project.file(NpmHelper.PACKAGE_JSON))) {
            val npmTest = project.tasks.create("clfNpmTest", NpmTask::class.java) { t ->
                t.group = AutoConfigureGradlePlugin.TASK_GROUP
                t.args.set(listOf("run", "test"))
                t.dependsOn(listOf(install, build))
                t.inputs.files(nodeExtension.npm.inputFiles)
                t.environment.put("GRADLE_BUILD", true.toString())
                t.environment.put("INTEGRATION_BUILD", isIntegrationBuild().toString())
            }
            project.tasks.getByName("test").dependsOn(npmTest)
        }
    }

    companion object {
        const val EXTENSION_NAME = "nodeConfigure"
    }
}