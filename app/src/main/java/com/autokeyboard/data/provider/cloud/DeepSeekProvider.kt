package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.flow.Flow

/**
 * DeepSeek Provider
 * Base URL: https://api.deepseek.com/v1
 * Auth: Bearer token — OpenAI-compatible
 */
class DeepSeekProvider(private val apiKey: String) : AIProvider {

    override val id = "deepseek"
    override val displayName = "DeepSeek"
    override val isLocal = false
    override val iconName = "explore"
    override val description = "Advanced Reasoning"

    override val supportedModels = listOf(
        Model("deepseek-chat", "DeepSeek Chat", "General purpose", 128000, true),
        Model("deepseek-reasoner", "DeepSeek Reasoner", "Chain of thought", 128000, true)
    )

    private val client by lazy {
        OpenAICompatClient(baseUrl = "https://api.deepseek.com/v1", apiKey = apiKey)
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
        return client.chatCompletion("deepseek-chat", "You suggest emojis.", prompt)
            .map { ResponseParser.parse(it).emojis }
    }

    override suspend fun validateApiKey(key: String): Boolean {
        return OpenAICompatClient("https://api.deepseek.com/v1", key).validateKey()
    }
}
