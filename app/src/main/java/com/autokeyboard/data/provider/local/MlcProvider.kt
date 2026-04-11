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
 * MLC LLM universal local runtime (advanced/power users).
 * Supports any MLC-compiled model: Llama-3.1-8B, Gemma-2-2B, Mistral-7B.
 */
class MlcProvider(
    private val modelManager: LocalModelManager
) : AIProvider {
    override val id = "mlc_local"
    override val displayName = "MLC LLM (Advanced)"
    override val isLocal = true
    override val iconName = "developer_mode"
    override val description = "Universal local runtime"

    override val supportedModels = listOf(
        Model("llama-3.1-8b-mlc", "Llama 3.1 8B", "MLC compiled", 8192),
        Model("gemma-2-2b-mlc", "Gemma 2 2B", "MLC compiled", 4096),
        Model("mistral-7b-mlc", "Mistral 7B", "MLC compiled", 8192)
    )

    val localModels = listOf(
        LocalModel("llama-3.1-8b-mlc", "Llama 3.1 8B MLC", "mlc_local", 4.0f, 6144, "", "MLC", "Q4"),
        LocalModel("gemma-2-2b-mlc", "Gemma 2 2B MLC", "mlc_local", 1.5f, 2048, "", "MLC", "Q4"),
        LocalModel("mistral-7b-mlc", "Mistral 7B MLC", "mlc_local", 3.8f, 5120, "", "MLC", "Q4")
    )

    override suspend fun rewrite(
        input: String, tone: Tone, customPrompt: String?, model: String
    ): Result<List<String>> = withContext(Dispatchers.Default) {
        try {
            val lm = localModels.find { it.id == model } ?: localModels.first()
            if (!modelManager.isModelDownloaded(lm)) {
                return@withContext Result.failure(IllegalStateException("Model not downloaded"))
            }
            // TODO: Integrate MLC Android runtime (mlc4j)
            val response = "MLC inference placeholder — mlc4j integration required"
            Result.success(ResponseParser.parse(response).variants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun streamRewrite(
        input: String, tone: Tone, customPrompt: String?, model: String
    ): Flow<String> = flow {
        emit("MLC streaming placeholder")
    }.flowOn(Dispatchers.Default)

    override suspend fun suggestEmojis(input: String, tone: Tone) =
        Result.success(listOf("🔧", "⚡", "🖥️", "🧪", "🎛️"))

    override suspend fun validateApiKey(key: String) = true
}
