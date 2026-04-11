package com.autokeyboard.data.provider.local

import com.autokeyboard.data.model.LocalModel
import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Meta Llama 3.2 on-device via llama.cpp JNI bridge (shared with Qwen).
 * Models: 1B, 3B in various quantizations.
 */
class LlamaLocalProvider(
    private val modelManager: LocalModelManager,
    private val bridge: LlamaCppBridge = LlamaCppBridge()
) : AIProvider {
    override val id = "llama_local"
    override val displayName = "Llama 3.2 (On-Device)"
    override val isLocal = true
    override val iconName = "pets"
    override val description = "Meta's open model"

    override val supportedModels = listOf(
        Model("llama-3.2-1b-q8", "Llama 3.2 1B", "Compact, 1.1GB", 4096),
        Model("llama-3.2-3b-q4", "Llama 3.2 3B", "Better quality, 1.8GB", 4096)
    )

    val localModels = listOf(
        LocalModel("llama-3.2-1b-q8", "Llama 3.2 1B Q8", "llama_local", 1.1f, 1536, "", "LLAMACPP", "Q8"),
        LocalModel("llama-3.2-3b-q4", "Llama 3.2 3B Q4", "llama_local", 1.8f, 2560, "", "LLAMACPP", "Q4")
    )

    override suspend fun rewrite(
        input: String, tone: Tone, customPrompt: String?, model: String
    ): Result<List<String>> = withContext(Dispatchers.Default) {
        try {
            val lm = localModels.find { it.id == model } ?: localModels.first()
            if (!modelManager.isModelDownloaded(lm)) {
                return@withContext Result.failure(IllegalStateException("Model not downloaded"))
            }
            if (!bridge.isModelLoaded()) {
                bridge.loadModel(modelManager.getModelPath(lm), 4096, 4)
            }
            val prompt = LocalPromptTemplates.buildLlamaPrompt(input, tone, customPrompt)
            val response = bridge.generate(prompt, 512, 0.7f) ?: "Generation failed"
            Result.success(ResponseParser.parse(response).variants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun streamRewrite(
        input: String, tone: Tone, customPrompt: String?, model: String
    ): Flow<String> = kotlinx.coroutines.flow.callbackFlow {
        val lm = localModels.find { it.id == model } ?: localModels.first()
        if (!modelManager.isModelDownloaded(lm)) throw IllegalStateException("Not downloaded")
        if (!bridge.isModelLoaded()) bridge.loadModel(modelManager.getModelPath(lm), 4096, 4)
        val prompt = LocalPromptTemplates.buildLlamaPrompt(input, tone, customPrompt)
        bridge.generateStreaming(prompt, 512, 0.7f) { token -> trySend(token) }
        close()
        kotlinx.coroutines.channels.awaitClose { }
    }.flowOn(Dispatchers.Default)

    override suspend fun suggestEmojis(input: String, tone: Tone) =
        Result.success(listOf("🦙", "🔥", "💪", "✨", "🎯"))

    override suspend fun validateApiKey(key: String) = true
}
