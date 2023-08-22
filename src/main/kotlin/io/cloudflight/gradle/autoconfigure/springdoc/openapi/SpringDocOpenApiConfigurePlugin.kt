package io.cloudflight.gradle.autoconfigure.springdoc.openapi

import com.github.psxpaul.task.JavaExecFork
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
        val openApiTask = target.tasks.named("generateOpenApiDocs", OpenApiGeneratorTask::class)
        makeOpenApiTaskReactive(openApiTask, openapi)

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

    private fun makeOpenApiTaskReactive(openApiTask: TaskProvider<OpenApiGeneratorTask>, openapi: OpenApiExtension) {
        // for some reason the springdoc plugin reads the values from the extension during task initialization and uses that as convention for the task properties,
        // which results in some values being incorrect. Because of that we reconfigure the properties conventions to directly use the extension properties. And
        // add conventions to the extension properties to the expected default values see: https://github.com/springdoc/springdoc-openapi-gradle-plugin/blob/master/src/main/kotlin/org/springdoc/openapi/gradle/plugin/OpenApiGeneratorTask.kt#L46
        openApiTask.configure {
            it.apiDocsUrl.convention(openapi.apiDocsUrl)
            it.outputFileName.convention(openapi.outputFileName)
            it.groupedApiMappings.convention(openapi.groupedApiMappings)
            it.outputDir.convention(openapi.outputDir)
        }

        openapi.apiDocsUrl.convention("http://localhost:8080/v3/api-docs")
        openapi.outputFileName.convention("openapi.json")
        openapi.groupedApiMappings.convention(emptyMap())
        openapi.outputDir.convention(openApiTask.flatMap { it.project.layout.buildDirectory })
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
        val serverPort = freeServerSocketPort()
        val managementPort = freeServerSocketPort()
        val outputFileName = configureExtension.fileFormat.map { "${basename}.${it.extension}" }
        val docsUrl = openapi.outputFileName.map {
            val basePath = "http://localhost:${serverPort}/v3/api-docs"
            when {
                it.endsWith(".${OpenApiFormat.JSON.extension}") -> basePath
                it.endsWith(".${OpenApiFormat.YAML.extension}") -> "${basePath}.${OpenApiFormat.YAML.extension}"
                else -> throw UnsupportedFormatException("The provided openapi filename '${it}' ends in an unsupported extension. Make sure you use 'yaml' or 'json'")
            }
        }

        openapi.outputDir.set(target.layout.buildDirectory.dir("generated/resources/openapi"))
        openapi.outputFileName.set(outputFileName)
        openapi.apiDocsUrl.set(docsUrl)
        openapi.customBootRun {
            it.workingDir.set(target.layout.buildDirectory.dir("dummyForkedSpringBootWorkingDir"))
        }

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

