package io.cloudflight.gradle.autoconfigure.springdoc.openapi

import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.util.addApiDocumentationPublication
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springdoc.openapi.gradle.plugin.OpenApiExtension
import org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import java.net.ServerSocket

class SpringDocOpenApiConfigurePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(JavaConfigurePlugin::class.java)
        target.plugins.apply(SpringBootPlugin::class.java)
        target.plugins.apply(OpenApiGradlePlugin::class.java)

        val openapi = target.extensions.getByType(OpenApiExtension::class.java)
        configureOpenApiExtension(openapi, target, target.name)
        val openApiTask = target.tasks.named("generateOpenApiDocs")
        val json2Yaml: TaskProvider<out Task> =
            target.tasks.register("clfJsonToYaml", Json2YamlTask::class.java) { task ->
                with(openapi) {
                    task.inputFile.set(outputDir.file(outputFileName))
                    task.outputFile.set(outputDir.file(outputFileName.map { it.replace(".json", ".yaml") }))
                    task.dependsOn(openApiTask)
                }
            }

        target.tasks.register("clfGenerateOpenApiDocumentation") {
            it.dependsOn(json2Yaml)
        }

        target.afterEvaluate {
            configureJsonDocumentPublishing(openapi, target, openApiTask)
            configureYamlDocumentPublishing(target, openapi, json2Yaml)
        }
    }

    private fun configureOpenApiExtension(
        openapi: OpenApiExtension,
        target: Project,
        basename: String
    ) {
        with(openapi) {
            val serverPort = freeServerSocketPort()
            val managementPort = freeServerSocketPort()

            outputDir.set(target.layout.buildDirectory.dir("generated/resources/openapi"))
            logger.debug("outputDir={}", outputDir.get())
            outputFileName.set("${basename}.json")
            logger.debug("outputFileName={}", outputFileName.get())
            apiDocsUrl.set("http://localhost:${serverPort}/v3/api-docs")
            logger.debug("apiDocsUrl={}", apiDocsUrl.get())

            mapOf(
                "--server.port" to serverPort,
                "--management.server.port" to managementPort
            ).forEach { arg ->
                logger.debug("Set ${arg.key}=${arg.value}")
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

    private fun configureJsonDocumentPublishing(
        openapi: OpenApiExtension,
        target: Project,
        task: TaskProvider<Task>,
    ) {
        addApiDocumentationPublication(
            target,
            task,
            target.artifacts,
            openapi.outputDir.get().toString(),
            openapi.outputFileName.get().replace(".json", ""),
            "json"
        )
    }

    private fun configureYamlDocumentPublishing(
        target: Project,
        openapi: OpenApiExtension,
        task: TaskProvider<out Task>
    ) {
        addApiDocumentationPublication(
            target,
            task,
            target.artifacts,
            openapi.outputDir.get().toString(),
            openapi.outputFileName.get().replace(".json", ""),
            "yaml"
        )
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(SpringDocOpenApiConfigurePlugin::class.java)
    }
}

