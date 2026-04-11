package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.flow.Flow

/**
 * Mistral AI Provider
 * Base URL: https://api.mistral.ai/v1
 * Auth: Bearer token
 * OpenAI-compatible endpoint
 */
class MistralProvider(
    private val apiKey: String
) : AIProvider {

    override val id = "mistral"
    override val displayName = "Mistral AI"
    override val isLocal = false
    override val iconName = "air"
    override val description = "European AI Excellence"

    override val supportedModels = listOf(
        Model("mistral-large-latest", "Mistral Large", "Most capable", 128000, true),
        Model("mistral-small-latest", "Mistral Small", "Fast & efficient", 128000, true),
        Model("open-mistral-7b", "Mistral 7B", "Open-source base", 32768, true),
        Model("open-mixtral-8x7b", "Mixtral 8x7B", "MoE architecture", 32768, true)
    )

    private val client by lazy {
        OpenAICompatClient(baseUrl = "https://api.mistral.ai/v1", apiKey = apiKey)
    }

    override suspend fun rewrite(input: String, tone: Tone, customPrompt: String?, model: String): Result<List<String>> {
        val sys = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
        val usr = PromptBuilder.buildUserPrompt(input)
        return client.chatCompletion(model, sys, usr).map { ResponseParser.parse(it).variants }
    }

    override fun streamRewrite(input: String, tone: Tone, customPrompt: String?, model: String): Flow<String> {
        val sys = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
        val usr = PromptBuilder.buildUserPrompt(input)
        return client.streamChatCompletion(model, sys, usr)
    }

    override suspend fun suggestEmojis(input: String, tone: Tone): Result<List<String>> {
        val prompt = PromptBuilder.buildEmojiPrompt(input, tone)
        return client.chatCompletion("mistral-small-latest", "You suggest emojis.", prompt)
            .map { ResponseParser.parse(it).emojis }
    }

    override suspend fun validateApiKey(key: String): Boolean {
        return OpenAICompatClient("https://api.mistral.ai/v1", key).validateKey()
    }
}
