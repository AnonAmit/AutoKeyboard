package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.flow.Flow

/**
 * Groq Provider — Ultra-fast inference
 * Base URL: https://api.groq.com/openai/v1
 * Auth: Bearer token
 * OpenAI-compatible endpoint
 */
class GroqProvider(
    private val apiKey: String
) : AIProvider {

    override val id = "groq"
    override val displayName = "Groq"
    override val isLocal = false
    override val iconName = "bolt"
    override val description = "Ultra-fast Inference"

    override val supportedModels = listOf(
        Model("llama-3.3-70b-versatile", "Llama 3.3 70B", "Most capable", 131072, true),
        Model("llama-3.1-8b-instant", "Llama 3.1 8B", "Ultra-fast", 131072, true),
        Model("mixtral-8x7b-32768", "Mixtral 8x7B", "MoE balanced", 32768, true),
        Model("gemma2-9b-it", "Gemma 2 9B", "Google open model", 8192, true),
        Model("llama3-groq-70b-8192-tool-use-preview", "Llama 3 70B Tool Use", "Tool-enabled", 8192, true)
    )

    private val client by lazy {
        OpenAICompatClient(baseUrl = "https://api.groq.com/openai/v1", apiKey = apiKey)
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
        return client.chatCompletion("llama-3.1-8b-instant", "You suggest emojis.", prompt)
            .map { ResponseParser.parse(it).emojis }
    }

    override suspend fun validateApiKey(key: String): Boolean {
        return OpenAICompatClient("https://api.groq.com/openai/v1", key).validateKey()
    }
}
