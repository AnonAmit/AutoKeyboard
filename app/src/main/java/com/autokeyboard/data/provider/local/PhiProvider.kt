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
 * Microsoft Phi-3 on-device via ONNX Runtime.
 * Models: phi-3-mini-4k-instruct (2.3GB), phi-3.5-mini-instruct (2.4GB)
 */
class PhiProvider(private val modelManager: LocalModelManager) : AIProvider {
    override val id = "phi_local"
    override val displayName = "Phi-3 (On-Device)"
    override val isLocal = true
    override val iconName = "memory"
    override val description = "Microsoft's compact model"

    override val supportedModels = listOf(
        Model("phi-3-mini-4k-instruct", "Phi-3 Mini 4K", "4K context, 2.3GB", 4096),
        Model("phi-3.5-mini-instruct", "Phi-3.5 Mini", "Improved, 2.4GB", 4096)
    )

    val localModels = listOf(
        LocalModel("phi-3-mini-4k-instruct", "Phi-3 Mini 4K", "phi_local", 2.3f, 3072, "", "ONNX", "INT4"),
        LocalModel("phi-3.5-mini-instruct", "Phi-3.5 Mini", "phi_local", 2.4f, 3072, "", "ONNX", "INT4")
    )

    override suspend fun rewrite(
        input: String, tone: Tone, customPrompt: String?, model: String
    ): Result<List<String>> = withContext(Dispatchers.Default) {
        try {
            val lm = localModels.find { it.id == model } ?: localModels.first()
            if (!modelManager.isModelDownloaded(lm)) {
                return@withContext Result.failure(IllegalStateException("Model not downloaded"))
            }
            val prompt = LocalPromptTemplates.buildPhiPrompt(input, tone, customPrompt)
            val response = runOnnxInference(modelManager.getModelPath(lm), prompt)
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
        val prompt = LocalPromptTemplates.buildPhiPrompt(input, tone, customPrompt)
        emit(runOnnxInference(modelManager.getModelPath(lm), prompt))
    }.flowOn(Dispatchers.Default)

    override suspend fun suggestEmojis(input: String, tone: Tone) =
        Result.success(listOf("\uD83D\uDCA1", "\uD83D\uDD2C", "\uD83D\uDCDD", "✨", "\uD83C\uDFAF"))

    override suspend fun validateApiKey(key: String) = true

    private fun runOnnxInference(modelPath: String, prompt: String): String {
        // TODO: Integrate ONNX Runtime InferenceSession
        // Requires loading model into ai.onnxruntime.OrtSession
        return "Local Phi-3 inference placeholder"
    }
}
