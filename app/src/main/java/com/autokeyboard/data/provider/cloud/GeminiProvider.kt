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
 * Google Gemini Provider
 * Base URL: https://generativelanguage.googleapis.com/v1beta
 * Auth: API key as query param (?key=)
 * Unique API format — NOT OpenAI-compatible
 */
class GeminiProvider(
    private val apiKey: String
) : AIProvider {

    override val id = "gemini"
    override val displayName = "Gemini"
    override val isLocal = false
    override val iconName = "diamond"
    override val description = "Google AI Suite"

    override val supportedModels = listOf(
        Model("gemini-2.0-flash", "Gemini 2.0 Flash", "Latest fast model", 1048576, true),
        Model("gemini-1.5-pro", "Gemini 1.5 Pro", "Most capable", 2097152, true),
        Model("gemini-1.5-flash", "Gemini 1.5 Flash", "Fast & efficient", 1048576, true),
        Model("gemini-1.0-pro", "Gemini 1.0 Pro", "Stable release", 30720, true)
    )

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"

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
                putJsonArray("contents") {
                    addJsonObject {
                        put("role", "user")
                        putJsonArray("parts") {
                            addJsonObject { put("text", "$systemPrompt\n\n$userPrompt") }
                        }
                    }
                }
                putJsonObject("generationConfig") {
                    put("temperature", 0.7f)
                    put("maxOutputTokens", 1024)
                }
            }.toString()

            val url = "$baseUrl/models/$model:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string()
                    return@withContext Result.failure(IOException("Gemini API error ${response.code}: $err"))
                }

                val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty response"))
                val jsonResponse = json.parseToJsonElement(body).jsonObject
                val text = jsonResponse["candidates"]?.jsonArray
                    ?.firstOrNull()?.jsonObject
                    ?.get("content")?.jsonObject
                    ?.get("parts")?.jsonArray
                    ?.firstOrNull()?.jsonObject
                    ?.get("text")?.jsonPrimitive?.content
                    ?: return@withContext Result.failure(IOException("No text in response"))

                Result.success(ResponseParser.parse(text).variants)
            }
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
            putJsonArray("contents") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("parts") {
                        addJsonObject { put("text", "$systemPrompt\n\n$userPrompt") }
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("temperature", 0.7f)
                put("maxOutputTokens", 1024)
            }
        }.toString()

        val url = "$baseUrl/models/$model:streamGenerateContent?key=$apiKey&alt=sse"
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, object : EventSourceListener() {
                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    try {
                        val chunk = json.parseToJsonElement(data).jsonObject
                        val text = chunk["candidates"]?.jsonArray
                            ?.firstOrNull()?.jsonObject
                            ?.get("content")?.jsonObject
                            ?.get("parts")?.jsonArray
                            ?.firstOrNull()?.jsonObject
                            ?.get("text")?.jsonPrimitive?.content
                        if (text != null) trySend(text)
                    } catch (_: Exception) { }
                }

                override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                    close(t ?: IOException("Gemini stream failed"))
                }

                override fun onClosed(eventSource: EventSource) { close() }
            })

        awaitClose { eventSource.cancel() }
    }.flowOn(Dispatchers.IO)

    override suspend fun suggestEmojis(input: String, tone: Tone): Result<List<String>> {
        val prompt = PromptBuilder.buildEmojiPrompt(input, tone)
        return rewrite(prompt, Tone.CASUAL, null, "gemini-2.0-flash").map { variants ->
            ResponseParser.parse(variants.firstOrNull() ?: "").emojis
        }
    }

    override suspend fun validateApiKey(key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/models?key=$key"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) { false }
    }
}
