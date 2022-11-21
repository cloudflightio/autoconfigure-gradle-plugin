package io.cloudflight.gradle.autoconfigure.node

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePluginExtension
import io.cloudflight.gradle.autoconfigure.util.BuildUtils.isIntegrationBuild
import io.cloudflight.gradle.autoconfigure.util.EnvironmentUtils.isVerifyBuild
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin
import java.io.File

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
            npm.npmVersion.convention(NPM_VERSION)
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

        project.extensions.getByType(JavaConfigurePluginExtension::class).apply {
            createSourceArtifacts.set(false)
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

        project.tasks.create(NPM_BUILD_DEV_TASK_NAME, NpmTask::class.java) { t ->
            t.group = AutoConfigureGradlePlugin.TASK_GROUP
            t.args.set(listOf("run", "build:dev"))
            t.dependsOn(install)
        }

        val updateVersion = project.tasks.create(NPM_UPDATE_VERSION_TASK_NAME, NpmTask::class.java) { t ->
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

        val packageJsonFile = project.file(NpmHelper.PACKAGE_JSON)

        if (isIntegrationBuild() && !isVerifyBuild() && packageJsonFile.exists()) {
            install.dependsOn(updateVersion)
        }

        project.tasks.getByName(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(lint)

        //this prevents running npmBuild each time when a project is started via intellij
        project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).mustRunAfter(build)
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn(build)

        val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        val sourceSetMain = javaPluginExtension.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        sourceSetMain.java.srcDirs(NpmHelper.determineSourceDirs(project))
        sourceSetMain.output.dir(mapOf("builtBy" to build), nodeExtension.npm.destinationDir)

        val packageJson = project.file(NpmHelper.PACKAGE_JSON)

        if (NpmHelper.hasScript("test", packageJson)) {
            val npmTest = project.tasks.create(NPM_TEST_TASK_NAME, NpmTask::class.java) { t ->
                t.group = AutoConfigureGradlePlugin.TASK_GROUP
                t.args.set(listOf("run", "test"))
                t.dependsOn(listOf(install, build))
                t.inputs.files(nodeExtension.npm.inputFiles)
                t.environment.put("GRADLE_BUILD", true.toString())
                t.environment.put("INTEGRATION_BUILD", isIntegrationBuild().toString())
            }
            project.tasks.getByName("test").dependsOn(npmTest)
        }

        if (NpmHelper.hasScript("clean", packageJson)) {
            val npmClean = project.tasks.create(NPM_CLEAN_TASK_NAME, NpmTask::class.java) { t ->
                t.group = AutoConfigureGradlePlugin.TASK_GROUP
                t.args.set(listOf("run", "clean"))
            }
            project.tasks.getByName("clean").dependsOn(npmClean)
        }
    }

    companion object {
        const val NPM_CLEAN_TASK_NAME = "clfNpmClean"
        const val NPM_BUILD_TASK_NAME = "clfNpmBuild"
        const val NPM_BUILD_DEV_TASK_NAME = "clfNpmBuildDev"
        const val NPM_LINT_TASK_NAME = "clfNpmLint"
        const val NPM_TEST_TASK_NAME = "clfNpmTest"
        const val NPM_UPDATE_VERSION_TASK_NAME = "clfNpmUpdateVersion"

        private const val EXTENSION_NAME = "nodeConfigure"
    }
}