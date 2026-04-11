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
 * Qwen2.5 on-device via llama.cpp JNI bridge.
 * Models: 0.5B, 1.5B, 3B in various quantizations.
 */
class QwenProvider(
    private val modelManager: LocalModelManager,
    private val bridge: LlamaCppBridge = LlamaCppBridge()
) : AIProvider {
    override val id = "qwen_local"
    override val displayName = "Qwen (On-Device)"
    override val isLocal = true
    override val iconName = "translate"
    override val description = "Alibaba's compact model"

    override val supportedModels = listOf(
        Model("qwen2.5-0.5b-q8", "Qwen 2.5 0.5B", "Smallest, 500MB", 2048),
        Model("qwen2.5-1.5b-q6", "Qwen 2.5 1.5B", "Balanced, 1.1GB", 4096),
        Model("qwen2.5-3b-q4", "Qwen 2.5 3B", "Best quality, 1.8GB", 4096)
    )

    val localModels = listOf(
        LocalModel("qwen2.5-0.5b-q8", "Qwen 2.5 0.5B Q8", "qwen_local", 0.5f, 768, "", "LLAMACPP", "Q8"),
        LocalModel("qwen2.5-1.5b-q6", "Qwen 2.5 1.5B Q6", "qwen_local", 1.1f, 1536, "", "LLAMACPP", "Q6"),
        LocalModel("qwen2.5-3b-q4", "Qwen 2.5 3B Q4", "qwen_local", 1.8f, 2560, "", "LLAMACPP", "Q4")
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
                bridge.loadModel(modelManager.getModelPath(lm), 2048, 4)
            }
            val prompt = LocalPromptTemplates.buildQwenPrompt(input, tone, customPrompt)
            val response = bridge.generate(prompt, 512, 0.7f) ?: "Generation failed"
            Result.success(ResponseParser.parse(response).variants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun streamRewrite(
        input: String, tone: Tone, customPrompt: String?, model: String
    ): Flow<String> = flow {
        val lm = localModels.find { it.id == model } ?: localModels.first()
        if (!modelManager.isModelDownloaded(lm)) throw IllegalStateException("Not downloaded")
        if (!bridge.isModelLoaded()) bridge.loadModel(modelManager.getModelPath(lm), 2048, 4)
        val prompt = LocalPromptTemplates.buildQwenPrompt(input, tone, customPrompt)
        bridge.generateStreaming(prompt, 512, 0.7f) { token -> trySend(token) }
    }.flowOn(Dispatchers.Default)

    override suspend fun suggestEmojis(input: String, tone: Tone) =
        Result.success(listOf("🌐", "📖", "✍️", "💬", "🎯"))

    override suspend fun validateApiKey(key: String) = true
}
