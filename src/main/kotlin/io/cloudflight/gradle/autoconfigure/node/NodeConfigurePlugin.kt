package io.cloudflight.gradle.autoconfigure.node

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.apply
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.getByType
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePluginExtension
import io.cloudflight.gradle.autoconfigure.util.BuildUtils.isIntegrationBuild
import org.gradle.api.Plugin
import org.gradle.api.Project
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
            npmVersion.convention(npm.npmVersion)
            destinationDir.convention(npm.destinationDir)
            inputFiles.convention(npm.inputFiles)
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
        node.npmVersion.set(nodeExtension.npmVersion)


        // TODO create a setupYARN
        /*        val setVersion = project.tasks.create("setYarnVersion", YarnTask::class.java) {t->
            t.yarnCommand.set(listOf("set", "version", "berry"))
        }*/

        /*val setVersion = project.tasks.create("importVersionPlugin", YarnTask::class.java) {t->
            t.yarnCommand.set(listOf("plugin", "import", "version"))
        }*/


        if (project.file(".yarnrc.yml").exists()) {
            YarnConfiguration.apply(project, nodeExtension)
        } else {
            NpmConfiguration.apply(project, nodeExtension)
        }
    }

    companion object {
        const val NPM_BUILD_TASK_NAME = "clfNpmBuild"
        const val NPM_LINT_TASK_NAME = "clfNpmLint"

        const val YARN_BUILD_TASK_NAME = "clfYarnBuild"
        const val YARN_LINT_TASK_NAME = "clfYarnLinz"

        private const val EXTENSION_NAME = "nodeConfigure"
    }
}