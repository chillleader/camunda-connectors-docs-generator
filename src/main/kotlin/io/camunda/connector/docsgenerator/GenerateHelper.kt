package io.camunda.connector.docsgenerator

import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService

class GenerateHelper(private val openai: OpenAiService) : Helper<String> {

    override fun apply(input: String?, options: Options): String {
        val prompt = input
            ?: throw IllegalArgumentException(
                "Prompt required. Usage: {{generate 'prompt' [additional context]}}"
            )
        val additionalContext: Any = objectMapper.writeValueAsString(options.param(0)) ?: ""

        val chat = listOf(
            ChatMessage(ChatMessageRole.SYSTEM.value(), systemInstruction),
            ChatMessage(
                ChatMessageRole.USER.value(), """
                    Given the following information: $additionalContext
                    Generate a documentation-style text that answers the query below:
                    $prompt
        """.trimIndent()
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