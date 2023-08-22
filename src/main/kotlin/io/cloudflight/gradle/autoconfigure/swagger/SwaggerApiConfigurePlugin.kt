package io.cloudflight.gradle.autoconfigure.swagger

import com.benjaminsproule.swagger.gradleplugin.GenerateSwaggerDocsTask
import com.benjaminsproule.swagger.gradleplugin.classpath.ClassFinder
import com.benjaminsproule.swagger.gradleplugin.generator.GeneratorFactory
import com.benjaminsproule.swagger.gradleplugin.model.ApiSourceExtension
import com.benjaminsproule.swagger.gradleplugin.model.InfoExtension
import com.benjaminsproule.swagger.gradleplugin.model.SwaggerExtension
import com.benjaminsproule.swagger.gradleplugin.reader.ReaderFactory
import com.benjaminsproule.swagger.gradleplugin.validator.*
import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin
import io.cloudflight.gradle.autoconfigure.java.JavaConfigurePlugin
import io.cloudflight.gradle.autoconfigure.util.addApiDocumentationPublication
import org.gradle.api.*
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.classloader.MultiParentClassLoader
import java.io.File
import java.net.URLClassLoader

class SwaggerApiConfigurePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(JavaConfigurePlugin::class.java)

        val swagger = target.extensions.create(SwaggerExtension.EXTENSION_NAME, SwaggerExtension::class.java, target)

        target.afterEvaluate {

            val jarTask = target.tasks.getByName(JavaPlugin.JAR_TASK_NAME)

            if (swagger.apiSourceExtensions.isEmpty()) {
                swagger.apiSourceExtensions.add(ApiSourceExtension(target))
            }
            if (swagger.apiSourceExtensions.size > 1) {
                throw GradleException("only one apiSource is supported")
            }
            with(swagger.apiSourceExtensions.first()) {
                info = InfoExtension(target)
                info.title = target.name
                springmvc = true
                outputFormats = listOf("json", YAML)
                if (swaggerDirectory == null) {
                    swaggerDirectory = target.layout.buildDirectory
                        .dir("generated/resources/openapi").get().asFile.absolutePath
                }
                // only override if it's the default value
                if (swaggerFileName == "swagger") {
                    swaggerFileName = target.name
                }
            }

            val documentationTask =
                target.tasks.create("clfGenerateSwaggerDocumentation", GenerateSwaggerDocsTask::class.java)


            val extension = swagger.apiSourceExtensions.first()
            val outputs = extension.outputFormats.map {
                addApiDocumentationPublication(
                    documentationTask,
                    target.artifacts,
                    extension.swaggerDirectory,
                    extension.swaggerFileName,
                    it
                )
            }

            with(documentationTask) {
                group = AutoConfigureGradlePlugin.TASK_GROUP
                classFinder = ClassFinder(target)
                readerFactory = ReaderFactory(classFinder, ClassFinder(target, javaClass.classLoader))
                generatorFactory = GeneratorFactory(classFinder)
                apiSourceValidator = ApiSourceValidator(
                    InfoValidator(LicenseValidator()), SecurityDefinitionValidator(
                        ScopeValidator()
                    ), TagValidator(ExternalDocsValidator())
                )

                inputFiles = getFilesFromSourceSet(target)
                outputDirectories = listOf(target.file(swagger.apiSourceExtensions.first().swaggerDirectory))
                outputFile = outputs.map { it.file }

                doFirst(ConfigureSwaggerAction)
            }

            jarTask.dependsOn(documentationTask)
        }
    }

    internal object ConfigureSwaggerAction : Action<Task> {
        override fun execute(t: Task) {
            val project = t.project
            val swagger = project.extensions.getByType(SwaggerExtension::class.java)
            val extension = swagger.apiSourceExtensions.first()
            with(extension) {
                info.version = project.version.toString()
                if (locations == null) {
                    locations = listOf(
                        project.group.toString() + ".api",
                        project.group.toString() + ".api.dto",
                    )
                }
            }

            // Workaround for an issue in the generateSwaggerDocumentation plugin
            // check https://github.com/gigaSproule/swagger-gradle-plugin/issues/158#issuecomment-585823379
            val classLoader = getUrlClassLoader(project.buildscript.classLoader)
            if (classLoader != null) {
                listOf(
                    getFilesFromConfiguration(project, JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME),
                    getFilesFromConfiguration(project, JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME),
                    getFilesFromSourceSet(project)
                )
                    .flatten()
                    .forEach {
                        val method = classLoader.javaClass.methods.first { it.name == "addURL" }
                        method.invoke(classLoader, it.toURI().toURL())
                    }
            } else {
                throw GradleException("no classLoader could be found for the buildscript of $project")
            }
        }

        private fun getFilesFromConfiguration(project: Project, name: String): Set<File> {
            return project.configurations.getByName(name).files
        }

        private fun getUrlClassLoader(classLoader: ClassLoader?): URLClassLoader? {
            if (classLoader == null) {
                return null
            }

            if (classLoader is URLClassLoader) {
                return classLoader
            }

            if (classLoader is MultiParentClassLoader) {
                classLoader.parents.forEach {
                    val loader = getUrlClassLoader(it)
                    if (loader != null) return loader
                }
            }

            return getUrlClassLoader(classLoader.parent)
        }
    }

    companion object {
        private fun getFilesFromSourceSet(project: Project): FileCollection {
            val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
            val sourceSetMain = javaPluginExtension.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

            return sourceSetMain.output.classesDirs
        }
    }
}
