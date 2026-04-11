package com.autokeyboard.data.provider.local

import com.autokeyboard.data.model.LocalModel
import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.PromptBuilder
import com.autokeyboard.data.provider.ResponseParser
import com.autokeyboard.domain.provider.AIProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Google Gemma — on-device via MediaPipe LLM Inference API.
 * Models: gemma-3-1b-it (600MB), gemma-3-4b-it (2.3GB)
 */
class GemmaProvider(
    private val modelManager: LocalModelManager
) : AIProvider {

    override val id = "gemma_local"
    override val displayName = "Gemma (On-Device)"
    override val isLocal = true
    override val iconName = "diamond"
    override val description = "Google's lightweight model"

    override val supportedModels = listOf(
        Model("gemma-3-1b-it", "Gemma 3 1B", "Fastest, 600MB", 2048),
        Model("gemma-3-4b-it", "Gemma 3 4B", "Better quality, 2.3GB", 4096)
    )

    val localModels = listOf(
        LocalModel("gemma-3-1b-it", "Gemma 3 1B IT", "gemma_local", 0.6f, 1024, "", "MEDIAPIPE", "INT8"),
        LocalModel("gemma-3-4b-it", "Gemma 3 4B IT", "gemma_local", 2.3f, 3072, "", "MEDIAPIPE", "INT4")
    )

    // MediaPipe LLM Inference session (initialized on first use)
    private var isInitialized = false

    override suspend fun rewrite(input: String, tone: Tone, customPrompt: String?, model: String): Result<List<String>> =
        withContext(Dispatchers.Default) {
            try {
                val localModel = localModels.find { it.id == model } ?: localModels.first()
                if (!modelManager.isModelDownloaded(localModel)) {
                    return@withContext Result.failure(IllegalStateException("Model not downloaded"))
                }

                val prompt = buildLocalPrompt(input, tone, customPrompt)

                // MediaPipe inference would run here
                // For now, return a placeholder showing the infrastructure works
                val response = runMediaPipeInference(
                    modelManager.getModelPath(localModel),
                    prompt
                )

                Result.success(ResponseParser.parse(response).variants)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override fun streamRewrite(input: String, tone: Tone, customPrompt: String?, model: String): Flow<String> =
        flow {
            val localModel = localModels.find { it.id == model } ?: localModels.first()
            if (!modelManager.isModelDownloaded(localModel)) {
                throw IllegalStateException("Model not downloaded")
            }
            val prompt = buildLocalPrompt(input, tone, customPrompt)
            // Streaming inference via MediaPipe would emit tokens here
            emit(runMediaPipeInference(modelManager.getModelPath(localModel), prompt))
        }.flowOn(Dispatchers.Default)

    override suspend fun suggestEmojis(input: String, tone: Tone): Result<List<String>> {
        // Local models use a simpler emoji mapping
        return Result.success(getDefaultEmojis(tone))
    }

    override suspend fun validateApiKey(key: String): Boolean = true // No API key needed

    private fun buildLocalPrompt(input: String, tone: Tone, customPrompt: String?): String {
        val toneInst = if (tone == Tone.CUSTOM && !customPrompt.isNullOrBlank()) customPrompt
        else tone.systemPromptFragment
        return "<start_of_turn>user\n$toneInst\n\nRewrite: \"$input\"\n<end_of_turn>\n<start_of_turn>model\n"
    }

    private fun runMediaPipeInference(modelPath: String, prompt: String): String {
        // TODO: Integrate MediaPipe LLM Inference API
        // This requires com.google.mediapipe:tasks-genai dependency
        // and GPU delegate initialization
        return "Local inference placeholder — MediaPipe integration required"
    }

    private fun getDefaultEmojis(tone: Tone): List<String> = when (tone) {
        Tone.PROFESSIONAL -> listOf("💼", "📊", "✅", "🤝", "📧")
        Tone.FRIENDLY -> listOf("😊", "👋", "💛", "🌟", "🤗")
        Tone.CASUAL -> listOf("✌️", "😎", "👍", "🙃", "💬")
        Tone.FLIRTY -> listOf("😘", "✨", "❤️", "😏", "🌹")
        Tone.WITTY -> listOf("😏", "🎯", "💡", "🔥", "😂")
        Tone.POETIC -> listOf("🌙", "🎭", "🦋", "✨", "🌸")
        Tone.GEN_Z -> listOf("💀", "😭", "🔥", "✨", "🫡")
        Tone.FORMAL -> listOf("🎩", "📜", "⚖️", "🏛️", "✍️")
        Tone.CONCISE -> listOf("🎯", "⚡", "✂️", "📝", "✅")
        Tone.ENTHUSIASTIC -> listOf("🤩", "🎉", "🔥", "🙌", "✨")
        Tone.SARCASTIC -> listOf("🙃", "🙄", "😂", "💅", "🤡")
        Tone.CONFIDENT -> listOf("😎", "💯", "💪", "🚀", "👑")
        Tone.EMPATHETIC -> listOf("🤍", "🫂", "🥺", "🌸", "🤲")
        Tone.PERSUASIVE -> listOf("🗣️", "💡", "📈", "🤝", "🔥")
        Tone.PASSIVE_AGGRESSIVE -> listOf("☕", "🙃", "😊", "💅", "🐸")
        Tone.PIRATE -> listOf("🏴‍☠️", "⚓", "🦜", "💰", "🌊")
        Tone.CUSTOM -> listOf("✨", "💫", "🎨", "🎯", "💭")
    }
}
