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
import kotlinx.serialization.Serializable
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
 * Reusable client for all OpenAI-compatible API endpoints.
 * Used by: OpenAI, Groq, DeepSeek, Together AI, Custom/Self-hosted.
 */
class OpenAICompatClient(
    private val baseUrl: String,
    private val apiKey: String,
    private val extraHeaders: Map<String, String> = emptyMap(),
    private val authStyle: AuthStyle = AuthStyle.BEARER
) {
    enum class AuthStyle { BEARER, QUERY_PARAM, CUSTOM_HEADER }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Serializable
    data class ChatMessage(val role: String, val content: String)

    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
        val temperature: Float = 0.7f,
        val max_tokens: Int = 1024,
        val stream: Boolean = false
    )

    /**
     * Sends a non-streaming chat completion request.
     */
    suspend fun chatCompletion(
        model: String,
        systemPrompt: String,
        userMessage: String,
        temperature: Float = 0.7f
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(
                ChatRequest.serializer(),
                ChatRequest(
                    model = model,
                    messages = listOf(
                        ChatMessage("system", systemPrompt),
                        ChatMessage("user", userMessage)
                    ),
                    temperature = temperature,
                    stream = false
                )
            )

            val request = buildRequest("$baseUrl/chat/completions", requestBody)
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("API error ${response.code}: ${response.body?.string()}")
                )
            }

            val body = response.body?.string() ?: return@withContext Result.failure(
                IOException("Empty response body")
            )

            val jsonResponse = json.parseToJsonElement(body).jsonObject
            val content = jsonResponse["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
                ?: return@withContext Result.failure(IOException("No content in response"))

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a streaming chat completion request via SSE.
     */
    fun streamChatCompletion(
        model: String,
        systemPrompt: String,
        userMessage: String,
        temperature: Float = 0.7f
    ): Flow<String> = callbackFlow {
        val requestBody = json.encodeToString(
            ChatRequest.serializer(),
            ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("system", systemPrompt),
                    ChatMessage("user", userMessage)
                ),
                temperature = temperature,
                stream = true
            )
        )

        val request = buildRequest("$baseUrl/chat/completions", requestBody)

        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, object : EventSourceListener() {
                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    if (data == "[DONE]") {
                        close()
                        return
                    }
                    try {
                        val chunk = json.parseToJsonElement(data).jsonObject
                        val delta = chunk["choices"]?.jsonArray
                            ?.firstOrNull()?.jsonObject
                            ?.get("delta")?.jsonObject
                            ?.get("content")?.jsonPrimitive?.content
                        if (delta != null) {
                            trySend(delta)
                        }
                    } catch (_: Exception) { }
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?
                ) {
                    close(t ?: IOException("Stream failed: ${response?.code}"))
                }

                override fun onClosed(eventSource: EventSource) {
                    close()
                }
            })

        awaitClose { eventSource.cancel() }
    }.flowOn(Dispatchers.IO)

    /**
     * Validates the API key by making a lightweight models list request.
     */
    suspend fun validateKey(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .apply { addAuthHeaders(this) }
                .get()
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    private fun buildRequest(url: String, jsonBody: String): Request {
        return Request.Builder()
            .url(url)
            .apply { addAuthHeaders(this) }
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()
    }

    private fun addAuthHeaders(builder: Request.Builder) {
        when (authStyle) {
            AuthStyle.BEARER -> builder.addHeader("Authorization", "Bearer $apiKey")
            AuthStyle.QUERY_PARAM -> { /* handled in URL */ }
            AuthStyle.CUSTOM_HEADER -> { /* handled by extraHeaders */ }
        }
        extraHeaders.forEach { (key, value) ->
            builder.addHeader(key, value)
        }
    }
}
