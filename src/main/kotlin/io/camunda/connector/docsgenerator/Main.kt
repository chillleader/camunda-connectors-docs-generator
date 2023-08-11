package io.camunda.connector.docsgenerator

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jknack.handlebars.Handlebars
import com.theokanning.openai.service.OpenAiService
import java.nio.file.Paths
import java.time.Duration

val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

const val OUTPUT_FILE = "README.md"

val openAiKey = System.getenv("OPENAI_KEY")
    ?: throw IllegalArgumentException("OpenAI key is required, supply it in the environment variable OPENAI_KEY")

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Element template path is required, usage: java -jar <jar> <element template path>")
    val elementTemplatePath: String = args[0]

    val hbsTemplate = getHandlebars().compile("template")

    val elementTemplate = readElementTemplate(elementTemplatePath)
        .removeReservedProperties()
        .removeHiddenProperties()

    val name = elementTemplate.name
    val context = GeneratorContext(elementTemplate, name)
    val output = hbsTemplate.apply(context)
    Paths.get(OUTPUT_FILE).toFile().writeText(output)
    println("Documentation generated to $OUTPUT_FILE")
}

private fun getHandlebars(): Handlebars {
    val openai = OpenAiService(openAiKey, Duration.ofSeconds(60))
    return Handlebars().apply {
        registerHelper("generateGroup", GenerateGroupHelper(openai))
        registerHelper("generate", GenerateHelper(openai))
    }
}

private fun readElementTemplate(elementTemplatePath: String): ElementTemplate {
    val elementTemplateFile = Paths.get(elementTemplatePath).toFile()
    return objectMapper.readValue(elementTemplateFile, ElementTemplate::class.java)
}