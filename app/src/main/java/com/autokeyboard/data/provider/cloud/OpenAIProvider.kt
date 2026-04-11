package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * OpenAI Provider — GPT-4o, GPT-4o-mini, GPT-4-turbo, GPT-3.5-turbo
 * Base URL: https://api.openai.com/v1
 * Auth: Bearer token
 */
class OpenAIProvider(
    private val apiKey: String,
    private val customBaseUrl: String? = null
) : AIProvider {

    override val id = "openai"
    override val displayName = "OpenAI"
    override val isLocal = false
    override val iconName = "auto_fix"
    override val description = "GPT-4o Optimized"

    override val supportedModels = listOf(
        Model("gpt-4o", "GPT-4o", "Most capable model", 128000, true),
        Model("gpt-4o-mini", "GPT-4o Mini", "Fast & affordable", 128000, true),
        Model("gpt-4-turbo", "GPT-4 Turbo", "Previous gen flagship", 128000, true),
        Model("gpt-3.5-turbo", "GPT-3.5 Turbo", "Legacy fast model", 16385, true)
    )

    private val client by lazy {
        OpenAICompatClient(
            baseUrl = customBaseUrl ?: "https://api.openai.com/v1",
            apiKey = apiKey
        )
    }

    override suspend fun rewrite(
        input: String,
        tone: Tone,
        customPrompt: String?,
        model: String
    ): Result<List<String>> {
        val systemPrompt = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
        val userPrompt = PromptBuilder.buildUserPrompt(input)

        return client.chatCompletion(model, systemPrompt, userPrompt).map { response ->
            ResponseParser.parse(response).variants
        }
    }

    override fun streamRewrite(
        input: String,
        tone: Tone,
        customPrompt: String?,
        model: String
    ): Flow<String> {
        val systemPrompt = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
        val userPrompt = PromptBuilder.buildUserPrompt(input)
        return client.streamChatCompletion(model, systemPrompt, userPrompt)
    }

    override suspend fun suggestEmojis(input: String, tone: Tone): Result<List<String>> {
        val prompt = PromptBuilder.buildEmojiPrompt(input, tone)
        return client.chatCompletion("gpt-4o-mini", "You suggest emojis.", prompt).map { response ->
            ResponseParser.parse(response).emojis
        }
    }

    override suspend fun validateApiKey(key: String): Boolean {
        return OpenAICompatClient(
            baseUrl = customBaseUrl ?: "https://api.openai.com/v1",
            apiKey = key
        ).validateKey()
    }
}
