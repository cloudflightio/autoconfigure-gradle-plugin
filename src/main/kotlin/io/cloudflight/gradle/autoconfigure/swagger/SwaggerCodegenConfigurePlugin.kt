package io.cloudflight.gradle.autoconfigure.swagger

import io.cloudflight.gradle.autoconfigure.extentions.gradle.api.plugins.create
import io.cloudflight.gradle.autoconfigure.node.NodeConfigurePlugin.Companion.NPM_BUILD_TASK_NAME
import io.cloudflight.gradle.autoconfigure.node.NodeConfigurePlugin.Companion.NPM_LINT_TASK_NAME
import io.cloudflight.gradle.autoconfigure.node.NodeConfigurePlugin.Companion.YARN_BUILD_TASK_NAME
import io.cloudflight.gradle.autoconfigure.node.NodeConfigurePlugin.Companion.YARN_LINT_TASK_NAME
import io.cloudflight.gradle.autoconfigure.node.isNodeProject
import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.hidetake.gradle.swagger.generator.GenerateSwaggerCode
import org.hidetake.gradle.swagger.generator.ResolveSwaggerTemplate
import org.hidetake.gradle.swagger.generator.SwaggerGeneratorPlugin
import org.hidetake.gradle.swagger.generator.SwaggerSource
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File


class SwaggerCodegenConfigurePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(SwaggerGeneratorPlugin::class.java)
        project.configurations.maybeCreate(CONFIGURATION_SWAGGER_API).apply {
            isTransitive = false
            incoming.beforeResolve {
                incoming.dependencies.forEach { dependency ->
                    (dependency as ModuleDependency).artifact { a ->
                        a.name = dependency.name
                        a.type = YAML
                        a.extension = YAML
                        a.classifier = SWAGGER_CLASSIFIER
                    }
                }
            }
        }

        val extension = project.extensions.create(EXTENSION_NAME, SwaggerCodegenConfigurePluginExtension::class).apply {
            swaggerCodegenCliVersion.convention("3.0.34")
            nodeSwaggerGenerator.convention("typescript-angular")
        }

        // generate api after evaluation of all projects, i.e. after 'swaggerApi' dependencies have been processed
        project.gradle.projectsEvaluated {
            configureSwaggerCodegen(project, extension)
        }
    }

    private fun configureSwaggerCodegen(project: Project, extension: SwaggerCodegenConfigurePluginExtension) {
        with(project) {
            // resolve swagger api project
            val swaggerApi = configurations.getByName(CONFIGURATION_SWAGGER_API)
            val apiDescriptors = resolveSwaggerApiProject(swaggerApi)

            val clean = tasks.getByName(LifecycleBasePlugin.CLEAN_TASK_NAME) as Delete
            val compileJava = tasks.findByName(JavaPlugin.COMPILE_JAVA_TASK_NAME) as JavaCompile?
            val sourcesJar = tasks.findByName("sourcesJar")

            val compileKotlin = tasks.findByName("compileKotlin") as KotlinCompile?
            val generateSwaggerCode = tasks.getByName("generateSwaggerCode") as GenerateSwaggerCode
            val npmBuild = tasks.findByName(NPM_BUILD_TASK_NAME)
            val yarnBuild = tasks.findByName(YARN_BUILD_TASK_NAME)
            val resolveSwaggerTemplate = tasks.findByName("resolveSwaggerTemplate") as ResolveSwaggerTemplate?

            if (resolveSwaggerTemplate != null) {
                resolveSwaggerTemplate.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }

            // ensure code generator
            val swaggerCodegen = configurations.getByName(CONFIGURATION_SWAGGER_CODEGEN)
            if (swaggerCodegen.dependencies.isEmpty()) {
                // only add swagger code generator if configuration is empty, this makes it possible to overwrite the generator on a per-project level simply by adding a 'swaggerCodegen' dependency
                dependencies.add(
                    CONFIGURATION_SWAGGER_CODEGEN,
                    "io.swagger.codegen.v3:swagger-codegen-cli:${extension.swaggerCodegenCliVersion.get()}"
                )
            }

            if (npmBuild != null) {
                npmBuild.dependsOn(generateSwaggerCode)
                val npmLint = tasks.findByName(NPM_LINT_TASK_NAME)
                npmLint?.dependsOn(generateSwaggerCode)
            } else if (yarnBuild != null) {
                yarnBuild.dependsOn(generateSwaggerCode)
                val yarnLint = tasks.findByName(YARN_LINT_TASK_NAME)
                yarnLint?.dependsOn(generateSwaggerCode)
            } else if (compileKotlin != null) {
                compileKotlin.dependsOn(generateSwaggerCode)
            } else if (compileJava != null) {
                compileJava.dependsOn(generateSwaggerCode)
            }
            sourcesJar?.dependsOn(generateSwaggerCode)

            val swaggerSources =
                extensions.getByName(EXTENSION_SWAGGER_SOURCES) as NamedDomainObjectContainer<SwaggerSource>

            apiDescriptors.forEach { apiDescriptor ->
                // find tasks
                val swaggerProject = apiDescriptor.swaggerProject ?: project
                val apiJar =
                    if (apiDescriptor.swaggerProject != null) apiDescriptor.swaggerProject.tasks.findByName(JavaPlugin.JAR_TASK_NAME) as Jar else null

                // ensure build order
                if (apiJar != null) {
                    project.tasks.forEach {
                        addDepends(it, apiJar)
                    }
                    project.tasks.whenTaskAdded {
                        addDepends(it, apiJar)
                    }
                }

                LOG.info("Swagger plugin generates code from '${apiDescriptor.swaggerPath}' for configuration '${apiDescriptor.swaggerName}'")

                swaggerSources.maybeCreate(apiDescriptor.swaggerName).also {
                    it.setInputFile(apiDescriptor.swaggerPath)
                    with(it.code) {
                        if (outputDir == null) {
                            outputDir = project.file("${buildDir}/generated-sources/${apiDescriptor.swaggerName}")
                        }
                        if (templateDir == null) {
                            val template = project.configurations.getByName(CONFIGURATION_SWAGGER_TEMPLATE)
                            if (template.dependencies.isNotEmpty()) {
                                templateDir = project.file("${buildDir}/swagger-template")
                            }
                        }
                        if (components == null) {
                            components = mapOf(
                                "apis" to true,
                                "apiTests" to false,
                                "models" to true,
                                "supportingFiles" to true
                            )
                        }
                        val swaggerGenerator: String?
                        val swaggerLibrary: String?

                        val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
                        val sourceSetMain = javaPluginExtension.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                        val sourceSetTest = javaPluginExtension.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)

                        if (isNodeProject(project)) {
                            swaggerGenerator = extension.nodeSwaggerGenerator.get()
                            swaggerLibrary = null
                            npmBuild?.inputs?.files(project.fileTree(outputDir))
                            yarnBuild?.inputs?.files(project.fileTree(outputDir))

                            if (!sourceSetMain.java.sourceDirectories.any { s -> outputDir.absolutePath.startsWith(s.absolutePath) }) {
                                sourceSetMain.java.srcDirs(outputDir)
                            }
                        } else {
                            swaggerGenerator = "spring"
                            swaggerLibrary = if (isClientProject(project)) "spring-cloud" else "spring-boot"

                            sourceSetMain.java.srcDirs("${outputDir}/src/main/java")
                            sourceSetTest.java.srcDirs("${outputDir}/src/test/java")
                        }

                        if (language == null) {
                            language = swaggerGenerator
                        }
                        if (library == null) {
                            library = swaggerLibrary
                        }
                    }
                    if (isNodeProject(project)) {
                        maybeSetAdditionalProperty(it.code, "delegatePattern", false.toString())
                    }

                    if (it.code.library == "spring-cloud") {
                        maybeSetComponents(it.code, "supportingFiles", false.toString())

                        maybeSetAdditionalProperty(it.code, "defaultInterfaces", false.toString())
                        maybeSetAdditionalProperty(it.code, "delegatePattern", false.toString())
                        maybeSetAdditionalProperty(it.code, "interfaceOnly", true.toString())
                    }

                    /**
                     * this is needed because of issues with JDK17 and swagger-codegen-cli and the dependency handlebars
                     * once the following issue is resolved this can be removed again: https://github.com/swagger-api/swagger-codegen/issues/10966
                     */
                    if (it.code.jvmArgs == null) it.code.jvmArgs = mutableListOf()
                    it.code.jvmArgs.add("--add-opens=java.base/java.util=ALL-UNNAMED")

                    maybeSetAdditionalProperty(it.code, "title", apiDescriptor.swaggerName)

                    maybeSetAdditionalProperty(it.code, "invokerPackage", swaggerProject.group.toString())
                    maybeSetAdditionalProperty(
                        it.code,
                        "apiPackage",
                        it.code.additionalProperties["invokerPackage"] + ".api"
                    )
                    maybeSetAdditionalProperty(
                        it.code,
                        "modelPackage",
                        it.code.additionalProperties["invokerPackage"] + ".api"
                    )

                    maybeSetAdditionalProperty(it.code, "dateLibrary", "java8")
                    maybeSetAdditionalProperty(it.code, "java8", true.toString())

                    maybeSetAdditionalProperty(it.code, "delegatePattern", true.toString())

                    maybeSetAdditionalProperty(it.code, "hideGenerationTimestamp", true.toString())

                    maybeSetAdditionalProperty(it.code, "useBeanValidation", true.toString())
                    maybeSetAdditionalProperty(it.code, "useTags", true.toString())

                    generateSwaggerCode.inputs.file(apiDescriptor.swaggerPath!!)
                    generateSwaggerCode.outputs.dir(it.code.outputDir)

                    clean.doFirst { t ->
                        project.delete(it.code.outputDir)
                    }
                }
            }
        }
    }

    private fun addDepends(t1: Task, t2: Task) {
        if (t1 is GenerateSwaggerCode) {
            t1.dependsOn(t2)
        }
    }

    private fun maybeSetAdditionalProperty(code: GenerateSwaggerCode, property: String, value: String) {
        if (code.additionalProperties == null) code.additionalProperties = mutableMapOf()
        if (!code.additionalProperties.containsKey(property)) code.additionalProperties[property] = value

        LOG.debug("property: '$property', value: '${code.additionalProperties[property]}', default: '$value'")
    }

    private fun maybeSetComponents(code: GenerateSwaggerCode, property: String, value: String) {
        if (code.components == null) code.components = mutableMapOf<String, String>()

        val map = code.components as MutableMap<String, String>
        if (map[property] == null) {
            map[property] = value
        }
    }

    private fun isClientProject(project: Project): Boolean {
        return project.name.endsWith("-client")
    }

    private fun resolveSwaggerApiProject(swaggerApi: Configuration): List<SwaggerApiDescriptor> {
        val descriptors = mutableListOf<SwaggerApiDescriptor>()

        swaggerApi.dependencies.forEach { dependency ->
            var swaggerProject: Project? = null
            var swaggerPath: File? = null

            if (dependency is ProjectDependency) {

                swaggerProject = dependency.dependencyProject

                val configuration = swaggerProject.configurations.getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME)


                // look for yaml file first
                configuration.artifacts.forEach { artifact ->
                    if (artifact.classifier == SWAGGER_CLASSIFIER && artifact.type == YAML) {
                        swaggerPath = artifact.file
                    }
                }

                // look for json file
                if (swaggerPath == null) {
                    configuration.artifacts.forEach { artifact ->
                        if (artifact.classifier == SWAGGER_CLASSIFIER && artifact.type == "json") {
                            swaggerPath = artifact.file
                        }
                    }
                }
            } else {
                val resolvedArtifacts = swaggerApi.resolvedConfiguration.resolvedArtifacts
                swaggerPath =
                    resolvedArtifacts.find { it.name == dependency.name && it.classifier == SWAGGER_CLASSIFIER && it.type == YAML }?.file

                if (swaggerPath == null) {
                    swaggerPath =
                        resolvedArtifacts.find { it.name == dependency.name && it.classifier == SWAGGER_CLASSIFIER && it.type == "json" }?.file
                }
            }

            if (swaggerProject == null && swaggerPath == null) {
                throw GradleException("swaggerApi dependency has to be a project dependency or swaggerPath has to be defined")
            }

            descriptors.add(
                SwaggerApiDescriptor(
                    // replace invalid task name characters
                    swaggerName = dependency.name,
                    swaggerProject = swaggerProject,
                    swaggerPath = swaggerPath
                )
            )
        }

        return descriptors
    }


    companion object {
        private const val CONFIGURATION_SWAGGER_API = "swaggerApi"
        private const val CONFIGURATION_SWAGGER_CODEGEN = "swaggerCodegen"
        private const val CONFIGURATION_SWAGGER_TEMPLATE = "swaggerTemplate"

        private const val EXTENSION_NAME = "swaggerCodgenConfigure"
        private const val EXTENSION_SWAGGER_SOURCES = "swaggerSources"

        private val LOG = Logging.getLogger("io.cloudflight.gradle.swagger")
    }
}