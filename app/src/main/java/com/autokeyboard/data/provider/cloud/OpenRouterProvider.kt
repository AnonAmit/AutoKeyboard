package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.flow.Flow

/**
 * OpenRouter Provider — unified gateway to 100+ models
 * Base URL: https://openrouter.ai/api/v1
 * Auth: Bearer token + extra headers
 * OpenAI-compatible endpoint
 */
class OpenRouterProvider(
    private val apiKey: String
) : AIProvider {

    override val id = "openrouter"
    override val displayName = "OpenRouter"
    override val isLocal = false
    override val iconName = "route"
    override val description = "100+ Models Gateway"

    override val supportedModels = listOf(
        Model("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B", "Meta's best open model", 131072, true),
        Model("google/gemini-flash-1.5", "Gemini Flash 1.5", "Google's fast model", 1048576, true),
        Model("anthropic/claude-3.5-haiku", "Claude 3.5 Haiku", "Anthropic's fast model", 200000, true),
        Model("mistralai/mistral-7b-instruct", "Mistral 7B", "Open-source", 32768, true),
        Model("openai/gpt-4o-mini", "GPT-4o Mini", "OpenAI via router", 128000, true),
        Model("deepseek/deepseek-chat", "DeepSeek Chat", "DeepSeek via router", 128000, true)
    )

    private val client by lazy {
        OpenAICompatClient(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = apiKey,
            extraHeaders = mapOf(
                "HTTP-Referer" to "https://autokeyboard.app",
                "X-Title" to "AutoKeyboard"
            )
        )
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
        return client.chatCompletion("openai/gpt-4o-mini", "You suggest emojis.", prompt)
            .map { ResponseParser.parse(it).emojis }
    }

    override suspend fun validateApiKey(key: String): Boolean {
        return OpenAICompatClient(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = key,
            extraHeaders = mapOf(
                "HTTP-Referer" to "https://autokeyboard.app",
                "X-Title" to "AutoKeyboard"
            )
        ).validateKey()
    }
}
