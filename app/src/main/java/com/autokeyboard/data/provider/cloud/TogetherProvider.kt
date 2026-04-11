package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.flow.Flow

/**
 * Together AI Provider
 * Base URL: https://api.together.xyz/v1
 * Auth: Bearer token — OpenAI-compatible
 */
class TogetherProvider(private val apiKey: String) : AIProvider {

    override val id = "together"
    override val displayName = "Together AI"
    override val isLocal = false
    override val iconName = "group"
    override val description = "Open-Source Models"

    override val supportedModels = listOf(
        Model("meta-llama/Llama-3-70b-chat-hf", "Llama 3 70B", "Meta flagship", 8192, true),
        Model("mistralai/Mixtral-8x7B-Instruct-v0.1", "Mixtral 8x7B", "MoE model", 32768, true),
        Model("google/gemma-2-27b-it", "Gemma 2 27B", "Google open model", 8192, true),
        Model("Qwen/Qwen2.5-72B-Instruct", "Qwen 2.5 72B", "Alibaba's best", 32768, true)
    )

    private val client by lazy {
        OpenAICompatClient(baseUrl = "https://api.together.xyz/v1", apiKey = apiKey)
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
        return client.chatCompletion("meta-llama/Llama-3-70b-chat-hf", "You suggest emojis.", prompt)
            .map { ResponseParser.parse(it).emojis }
    }

    override suspend fun validateApiKey(key: String): Boolean {
        return OpenAICompatClient("https://api.together.xyz/v1", key).validateKey()
    }
}
