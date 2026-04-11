package com.autokeyboard.data.provider.cloud

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Anthropic Claude Provider
 * Base URL: https://api.anthropic.com/v1
 * Auth: x-api-key header + anthropic-version: 2023-06-01
 * Unique Messages API format
 */
class ClaudeProvider(
    private val apiKey: String
) : AIProvider {

    override val id = "claude"
    override val displayName = "Claude"
    override val isLocal = false
    override val iconName = "psychology"
    override val description = "Anthropic Intelligence"

    override val supportedModels = listOf(
        Model("claude-opus-4-6", "Claude Opus 4.6", "Most powerful", 200000, true),
        Model("claude-sonnet-4-6", "Claude Sonnet 4.6", "Best balance", 200000, true),
        Model("claude-haiku-4-5", "Claude Haiku 4.5", "Fastest", 200000, true)
    )

    private val baseUrl = "https://api.anthropic.com/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun rewrite(
        input: String,
        tone: Tone,
        customPrompt: String?,
        model: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
            val userPrompt = PromptBuilder.buildUserPrompt(input)

            val requestBody = buildJsonObject {
                put("model", model)
                put("max_tokens", 1024)
                put("system", systemPrompt)
                putJsonArray("messages") {
                    addJsonObject {
                        put("role", "user")
                        put("content", userPrompt)
                    }
                }
            }.toString()

            val request = Request.Builder()
                .url("$baseUrl/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(IOException("Claude API error ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty response"))
            val jsonResponse = json.parseToJsonElement(body).jsonObject
            val text = jsonResponse["content"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
                ?: return@withContext Result.failure(IOException("No text in response"))

            Result.success(ResponseParser.parse(text).variants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun streamRewrite(
        input: String,
        tone: Tone,
        customPrompt: String?,
        model: String
    ): Flow<String> = callbackFlow {
        val systemPrompt = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
        val userPrompt = PromptBuilder.buildUserPrompt(input)

        val requestBody = buildJsonObject {
            put("model", model)
            put("max_tokens", 1024)
            put("stream", true)
            put("system", systemPrompt)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", userPrompt)
                }
            }
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, object : EventSourceListener() {
                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    if (type == "content_block_delta") {
                        try {
                            val chunk = json.parseToJsonElement(data).jsonObject
                            val text = chunk["delta"]?.jsonObject
                                ?.get("text")?.jsonPrimitive?.content
                            if (text != null) trySend(text)
                        } catch (_: Exception) { }
                    }
                }

                override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                    close(t ?: IOException("Claude stream failed"))
                }

                override fun onClosed(eventSource: EventSource) { close() }
            })

        awaitClose { eventSource.cancel() }
    }.flowOn(Dispatchers.IO)

    override suspend fun suggestEmojis(input: String, tone: Tone): Result<List<String>> {
        val prompt = PromptBuilder.buildEmojiPrompt(input, tone)
        return rewrite(prompt, Tone.CASUAL, null, "claude-haiku-4-5").map { variants ->
            ResponseParser.parse(variants.firstOrNull() ?: "").emojis
        }
    }

    override suspend fun validateApiKey(key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/messages")
                .addHeader("x-api-key", key)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(buildJsonObject {
                    put("model", "claude-haiku-4-5")
                    put("max_tokens", 1)
                    putJsonArray("messages") {
                        addJsonObject { put("role", "user"); put("content", "hi") }
                    }
                }.toString().toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (_: Exception) { false }
    }
}
