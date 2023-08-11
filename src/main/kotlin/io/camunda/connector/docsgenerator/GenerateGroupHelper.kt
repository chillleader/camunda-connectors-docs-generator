package io.camunda.connector.docsgenerator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService

class GenerateGroupHelper(private val openai: OpenAiService) : Helper<String> {
    override fun apply(input: String?, options: Options): String {
        val groupName = input
            ?: throw IllegalArgumentException(
                "Group name required. Usage: {{generateGroup 'group name' elementTemplate}}"
            )
        val elementTemplate =
            options.param(0) as? ElementTemplate ?: throw IllegalArgumentException(
                "Element template required. Usage: {{generateGroup 'group name' elementTemplate}}"
            )

        val relevantProperties = elementTemplate.properties.filter { it.group == groupName }
        val templateAsJson = jacksonObjectMapper().writeValueAsString(relevantProperties)

        val connectorName = elementTemplate.name
        println("Generating documentation for group $groupName")

        val chat = listOf(
            ChatMessage(ChatMessageRole.SYSTEM.value(), systemInstruction),
            ChatMessage(
                ChatMessageRole.USER.value(), """
                    You are working with a connector called $connectorName}.
                    Below is a part of the element template for this connector. 
                    You need to generate documentation.
                    
                    $templateAsJson""".trimIndent()
            )
        )

        return getResponse(chat)
    }

    private fun getResponse(input: List<ChatMessage>): String {
        val request = ChatCompletionRequest.builder()
            .model(MODEL)
            .temperature(0.1)
            .messages(input)
            .build()
        return openai.createChatCompletion(request).choices[0].message.content
    }
}