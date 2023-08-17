package io.cloudflight.gradle.autoconfigure.springdoc.openapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class Json2YamlTask : DefaultTask() {
    @get:InputFile
    val inputFile: RegularFileProperty = project.objects.fileProperty()

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun convert() {
        convert(inputFile.get().asFile, outputFile.get().asFile)
    }

    internal fun convert(input: File, output: File) {
        val json = Json.parseToJsonElement(
            input.readText(Charsets.UTF_8)
        )
        val jsonMap = json.jsonObject.toMap()
        val yamlFactory = YAMLFactory()
        val mapper = ObjectMapper(yamlFactory)
        mapper.writeValue(output, jsonMap)
    }
}
