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
 * Cohere Provider
 * Base URL: https://api.cohere.com/v2
 * Auth: Bearer token
 * Custom Chat API format
 */
class CohereProvider(
    private val apiKey: String
) : AIProvider {

    override val id = "cohere"
    override val displayName = "Cohere"
    override val isLocal = false
    override val iconName = "hub"
    override val description = "Enterprise NLP"

    override val supportedModels = listOf(
        Model("command-r-plus", "Command R+", "Most capable", 128000, true),
        Model("command-r", "Command R", "Balanced", 128000, true),
        Model("command-light", "Command Light", "Fast & light", 4096, true)
    )

    private val baseUrl = "https://api.cohere.com/v2"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun rewrite(input: String, tone: Tone, customPrompt: String?, model: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val sys = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
                val usr = PromptBuilder.buildUserPrompt(input)

                val requestBody = buildJsonObject {
                    put("model", model)
                    putJsonArray("messages") {
                        addJsonObject { put("role", "system"); put("content", sys) }
                        addJsonObject { put("role", "user"); put("content", usr) }
                    }
                }.toString()

                val request = Request.Builder()
                    .url("$baseUrl/chat")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Cohere error ${response.code}"))
                }

                val body = response.body?.string() ?: return@withContext Result.failure(IOException("Empty"))
                val jsonResp = json.parseToJsonElement(body).jsonObject
                val text = jsonResp["message"]?.jsonObject
                    ?.get("content")?.jsonArray
                    ?.firstOrNull()?.jsonObject
                    ?.get("text")?.jsonPrimitive?.content
                    ?: return@withContext Result.failure(IOException("No text"))

                Result.success(ResponseParser.parse(text).variants)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun streamRewrite(input: String, tone: Tone, customPrompt: String?, model: String): Flow<String> =
        callbackFlow {
            val sys = PromptBuilder.buildSystemPrompt(tone, customPrompt, 2)
            val usr = PromptBuilder.buildUserPrompt(input)

            val requestBody = buildJsonObject {
                put("model", model)
                put("stream", true)
                putJsonArray("messages") {
                    addJsonObject { put("role", "system"); put("content", sys) }
                    addJsonObject { put("role", "user"); put("content", usr) }
                }
            }.toString()

            val request = Request.Builder()
                .url("$baseUrl/chat")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val eventSource = EventSources.createFactory(client)
                .newEventSource(request, object : EventSourceListener() {
                    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                        try {
                            val chunk = json.parseToJsonElement(data).jsonObject
                            if (chunk["type"]?.jsonPrimitive?.content == "content-delta") {
                                val text = chunk["delta"]?.jsonObject
                                    ?.get("message")?.jsonObject
                                    ?.get("content")?.jsonObject
                                    ?.get("text")?.jsonPrimitive?.content
                                if (text != null) trySend(text)
                            }
                        } catch (_: Exception) { }
                    }

                    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                        close(t ?: IOException("Cohere stream failed"))
                    }

                    override fun onClosed(eventSource: EventSource) { close() }
                })

            awaitClose { eventSource.cancel() }
        }.flowOn(Dispatchers.IO)

    override suspend fun suggestEmojis(input: String, tone: Tone): Result<List<String>> {
        val prompt = PromptBuilder.buildEmojiPrompt(input, tone)
        return rewrite(prompt, Tone.CASUAL, null, "command-r").map { variants ->
            ResponseParser.parse(variants.firstOrNull() ?: "").emojis
        }
    }

    override suspend fun validateApiKey(key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer $key")
                .get().build()
            client.newCall(request).execute().isSuccessful
        } catch (_: Exception) { false }
    }
}
