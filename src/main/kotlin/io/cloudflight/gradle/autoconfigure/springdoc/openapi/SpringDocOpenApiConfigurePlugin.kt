package io.cloudflight.gradle.autoconfigure.springdoc.openapi

import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin.Companion.TASK_GROUP
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.tasks.named
import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.tasks.withType
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
import org.springdoc.openapi.gradle.plugin.OpenApiGeneratorTask
import org.springdoc.openapi.gradle.plugin.OpenApiGradlePlugin
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import java.net.ServerSocket

class SpringDocOpenApiConfigurePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(JavaConfigurePlugin::class.java)
        target.plugins.apply(SpringBootPlugin::class.java)
        target.plugins.apply(OpenApiGradlePlugin::class.java)

        val extension = target.extensions.create(EXTENSION_NAME, SpringDocOpenApiConfigureExtension::class.java)
        extension.fileFormat.convention(OpenApiFormat.YAML)
        extension.groupedApiMappings.convention(emptyMap())
        val openapi = target.extensions.getByType(OpenApiExtension::class.java)
        configureOpenApiExtension(openapi, extension, target, target.name)
        val openApiTask = target.tasks.named("generateOpenApiDocs", OpenApiGeneratorTask::class)

        val documentationTask = target.tasks.register("clfGenerateOpenApiDocumentation") {
            it.group = TASK_GROUP
            it.dependsOn(openApiTask)
        }

        target.tasks.withType(GenerateMavenPom::class) {
            it.dependsOn(documentationTask)
        }

        target.afterEvaluate {
            configureDocumentPublishing(openapi, target, openApiTask)
        }
    }

    private fun configureOpenApiExtension(
        openapi: OpenApiExtension,
        configureExtension: SpringDocOpenApiConfigureExtension,
        target: Project,
        basename: String
    ) {
        val serverPort = freeServerSocketPort()
        val managementPort = freeServerSocketPort()
        val outputFileName = configureExtension.fileFormat.map { "${basename}.${it.extension}" }
        val urlPrefix = "http://localhost:${serverPort}"
        val docsUrl = openapi.outputFileName.map {
            val basePath = "$urlPrefix/v3/api-docs"
            when {
                it.endsWith(".${OpenApiFormat.JSON.extension}") -> basePath
                it.endsWith(".${OpenApiFormat.YAML.extension}") -> "${basePath}.${OpenApiFormat.YAML.extension}"
                else -> throw UnsupportedFormatException("The provided openapi filename '${it}' ends in an unsupported extension. Make sure you use 'yaml' or 'json'")
            }
        }

        openapi.groupedApiMappings.set(
            configureExtension.groupedApiMappings.map { actualMap ->
                actualMap.mapKeys { "$urlPrefix${it.key}" }
            }
        )

        openapi.outputDir.set(target.layout.buildDirectory.dir("generated/resources/openapi"))
        openapi.outputFileName.set(outputFileName)
        openapi.apiDocsUrl.set(docsUrl)

        mapOf(
            "--server.port" to serverPort,
            "--management.server.port" to managementPort
        ).forEach { arg ->
            openapi.customBootRun.args.add("${arg.key}=${arg.value}")
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
        task: TaskProvider<out Task>,
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

