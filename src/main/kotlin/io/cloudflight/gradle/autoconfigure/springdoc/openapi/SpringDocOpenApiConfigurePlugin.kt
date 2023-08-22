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

        val documentationTask = target.tasks.register("clfGenerateOpenApiDocumentation") {
            it.group = TASK_GROUP
            it.dependsOn(json2Yaml)
        }

        target.tasks.withType(GenerateMavenPom::class) {
            it.dependsOn(documentationTask)
        }

        `setupWorkaroundFor#171`(target, openapi)

        target.afterEvaluate {
            configureJsonDocumentPublishing(openapi, target, openApiTask)
            configureYamlDocumentPublishing(target, openapi, json2Yaml)
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

        target.tasks.matching { dependingTaskNames.contains(it.name) }.configureEach {
            it.dependsOn(createDirTask)
        }

        forkedSpringBootRun.configure {
            it.dependsOn(createDirTask)
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
            outputFileName.set("${basename}.json")
            apiDocsUrl.set("http://localhost:${serverPort}/v3/api-docs")
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

