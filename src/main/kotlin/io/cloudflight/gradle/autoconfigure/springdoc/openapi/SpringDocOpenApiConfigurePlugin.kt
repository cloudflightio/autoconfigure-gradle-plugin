package io.cloudflight.gradle.autoconfigure.springdoc.openapi

import com.github.psxpaul.task.JavaExecFork
import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin.Companion.TASK_GROUP
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.named
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.withType
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.util.addApiDocumentationPublication
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.tasks.TaskProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springdoc.openapi.gradle.plugin.OpenApiExtension
import org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import java.net.ServerSocket
import java.nio.file.Files

class SpringDocOpenApiConfigurePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(JavaConfigurePlugin::class.java)
        target.plugins.apply(SpringBootPlugin::class.java)
        target.plugins.apply(OpenApiGradlePlugin::class.java)

        val extension = target.extensions.create(EXTENSION_NAME, SpringDocOpenApiConfigureExtension::class.java)
        extension.fileFormat.convention(OpenApiFormat.YAML);
        val openapi = target.extensions.getByType(OpenApiExtension::class.java)
        configureOpenApiExtension(openapi, extension, target, target.name)
        val openApiTask = target.tasks.named("generateOpenApiDocs")

        val documentationTask = target.tasks.register("clfGenerateOpenApiDocumentation") {
            it.group = TASK_GROUP
            it.dependsOn(openApiTask)
        }

        target.tasks.withType(GenerateMavenPom::class) {
            it.dependsOn(documentationTask)
        }

        `setupWorkaroundFor#171`(target, openapi)

        target.afterEvaluate {
            configureDocumentPublishing(openapi, target, openApiTask)
        }
    }

    private fun `setupWorkaroundFor#171`(target: Project, openapi: OpenApiExtension) {
        val forkedSpringBootRun = target.tasks.named("forkedSpringBootRun", JavaExecFork::class)

        val createDirTask = target.tasks.register("createDummyForkedSpringBootWorkingDir") { task ->
            // use same working dir resolution as plugin itself: https://github.com/springdoc/springdoc-openapi-gradle-plugin/blob/master/src/main/kotlin/org/springdoc/openapi/gradle/plugin/OpenApiGradlePlugin.kt#L98
            val workingDirProvider = openapi.customBootRun.workingDir.zip(forkedSpringBootRun) { dir, forked ->
                dir?.asFile ?: forked.workingDir
            }
            task.outputs.dir(workingDirProvider)
            task.doFirst {
                val workingDir = workingDirProvider.get()
                Files.createDirectories(workingDir.toPath())
            }
        }

        // these tasks also need to depend on the createDirTask since they somehow access the dummy folder as well
        val dependingTaskNames = setOf("resolveMainClassName", "processResources", "compileKotlin", "compileJava")

        target.tasks.matching { dependingTaskNames.contains(it.name) }.all {
            it.dependsOn(createDirTask)
        }

        forkedSpringBootRun.configure {
            it.dependsOn(createDirTask)
        }
    }

    private fun configureOpenApiExtension(
        openapi: OpenApiExtension,
        configureExtension: SpringDocOpenApiConfigureExtension,
        target: Project,
        basename: String
    ) {
        with(openapi) {
            val serverPort = freeServerSocketPort()
            val managementPort = freeServerSocketPort()
            val docsUrl = configureExtension.fileFormat.map {
                val basePath = "http://localhost:${serverPort}/v3/api-docs"
                when (it) {
                    OpenApiFormat.JSON -> basePath
                    else -> "${basePath}.${it.extension}"
                }
            }

            outputDir.set(target.layout.buildDirectory.dir("generated/resources/openapi"))
            outputFileName.set(configureExtension.fileFormat.map { "${basename}.${it.extension}" })
            apiDocsUrl.set(docsUrl)
            customBootRun {
                it.workingDir.set(target.layout.buildDirectory.dir("dummyForkedSpringBootWorkingDir"))
            }

            mapOf(
                "--server.port" to serverPort,
                "--management.server.port" to managementPort
            ).forEach { arg ->
                customBootRun.args.add("${arg.key}=${arg.value}")
            }
        }
    }

    private fun freeServerSocketPort(): Int {
        val serverSocket = ServerSocket(0)
        return serverSocket.use {
            serverSocket.localPort
        }
    }

    private fun configureDocumentPublishing(
        openapi: OpenApiExtension,
        target: Project,
        task: TaskProvider<Task>,
    ) {
        val format = openapi.outputFileName.map {
            if (it.endsWith(".${OpenApiFormat.YAML.extension}")) {
                OpenApiFormat.YAML
            } else if (it.endsWith(".${OpenApiFormat.JSON.extension}")) {
                OpenApiFormat.JSON
            } else {
                throw UnsupportedFormatException("The provided openapi filename '${it}' ends in an unsupported extension. Make sure you use 'yaml' or 'json'")
            }
        }

        val basename = openapi.outputFileName.zip(format) { fileName, format ->
            fileName.replace(".${format.extension}", "")
        }

        addApiDocumentationPublication(
            task,
            target.artifacts,
            openapi.outputDir,
            basename,
            format
        )
    }

    companion object {
        const val EXTENSION_NAME = "openApiConfigure"
        val logger: Logger = LoggerFactory.getLogger(SpringDocOpenApiConfigurePlugin::class.java)
    }
}

