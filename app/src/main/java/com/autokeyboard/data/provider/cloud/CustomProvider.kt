package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.flow.Flow

/**
 * Custom / Self-Hosted Provider
 * Base URL: Fully user-configurable (e.g., http://192.168.1.10:11434/v1 for Ollama)
 * Auth: Optional Bearer token
 * Assumes OpenAI-compatible endpoint
 */
class CustomProvider(
    private val apiKey: String,
    private val baseUrl: String,
    private val modelName: String
) : AIProvider {

    override val id = "custom"
    override val displayName = "Custom / Self-Hosted"
    override val isLocal = false
    override val iconName = "dns"
    override val description = "Your own endpoint"

    override val supportedModels: List<Model>
        get() = if (modelName.isNotBlank()) {
            listOf(Model(modelName, modelName, "User-specified model"))
        } else {
            listOf(Model("default", "Default Model", "Specify in settings"))
        }

    private val client by lazy {
        OpenAICompatClient(
            baseUrl = baseUrl.trimEnd('/'),
            apiKey = apiKey
        )
    }

    override suspend fun rewrite(input: String, tone: Tone, customPrompt: String?, model: String): Result<List<String>> {
        val sys = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
        val usr = PromptBuilder.buildUserPrompt(input)
        val actualModel = model.ifBlank { modelName }
        return client.chatCompletion(actualModel, sys, usr).map { ResponseParser.parse(it).variants }
    }

    override fun streamRewrite(input: String, tone: Tone, customPrompt: String?, model: String): Flow<String> {
        val sys = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
        val usr = PromptBuilder.buildUserPrompt(input)
        val actualModel = model.ifBlank { modelName }
        return client.streamChatCompletion(actualModel, sys, usr)
    }

    override suspend fun suggestEmojis(input: String, tone: Tone): Result<List<String>> {
        val prompt = PromptBuilder.buildEmojiPrompt(input, tone)
        val actualModel = modelName.ifBlank { "default" }
        return client.chatCompletion(actualModel, "You suggest emojis.", prompt)
            .map { ResponseParser.parse(it).emojis }
    }

    override suspend fun validateApiKey(key: String): Boolean {
        return OpenAICompatClient(baseUrl.trimEnd('/'), key).validateKey()
    }
}
