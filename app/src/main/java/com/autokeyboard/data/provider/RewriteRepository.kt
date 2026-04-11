package com.autokeyboard.data.provider

import com.autokeyboard.data.local.PreferencesManager
import com.autokeyboard.data.model.RewriteResult
import com.autokeyboard.data.model.RewriteState
import com.autokeyboard.data.model.Tone
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main repository for AI rewrite operations.
 * Implements the fallback chain: primary -> fallback1 -> fallback2 -> local -> error.
 */
@Singleton
class RewriteRepository @Inject constructor(
    private val registry: ProviderRegistry,
    private val preferencesManager: PreferencesManager
) {
    /**
     * Performs a rewrite with automatic fallback across configured providers.
     * Tries providers in user-configured priority order.
     */
    suspend fun rewrite(
        input: String,
        tone: Tone,
        customPrompt: String?,
        variantCount: Int = 2
    ): RewriteState {
        if (input.isBlank()) {
            return RewriteState.Error("No text to rewrite", canRetry = false)
        }

        val providerOrder = preferencesManager.providerPriority.first()
        val orderedIds = if (providerOrder.isNotEmpty()) {
            providerOrder
        } else {
            registry.allProviderIds.filter { registry.isProviderConfigured(it) }
        }

        if (orderedIds.isEmpty()) {
            return RewriteState.Error("No AI providers configured. Go to Settings to add one.", canRetry = false)
        }

        var lastError: String = "Unknown error"
        val startTime = System.currentTimeMillis()

        for (providerId in orderedIds) {
            val provider = registry.getProvider(providerId) ?: continue
            val selectedModel = provider.supportedModels.firstOrNull()?.id ?: continue

            try {
                val result = provider.rewrite(input, tone, customPrompt, selectedModel)
                if (result.isSuccess) {
                    val variants = result.getOrThrow()
                    if (variants.isNotEmpty()) {
                        // Get emojis
                        val emojis = try {
                            provider.suggestEmojis(input, tone).getOrDefault(emptyList())
                        } catch (_: Exception) {
                            getDefaultEmojis(tone)
                        }

                        val latency = System.currentTimeMillis() - startTime
                        return RewriteState.Success(
                            RewriteResult(
                                variants = variants.take(variantCount),
                                emojis = emojis.take(5),
                                tone = tone,
                                providerId = providerId,
                                latencyMs = latency
                            )
                        )
                    }
                }
                lastError = result.exceptionOrNull()?.message ?: "Empty response"
            } catch (e: Exception) {
                lastError = e.message ?: "Provider $providerId failed"
            }
        }

        return RewriteState.Error("All providers failed: $lastError")
    }

    /**
     * Performs a streaming rewrite via the primary provider.
     */
    fun streamRewrite(
        input: String,
        tone: Tone,
        customPrompt: String?
    ): Flow<RewriteState> = flow {
        emit(RewriteState.Loading)

        val providerOrder = preferencesManager.providerPriority.first()
        val orderedIds = providerOrder.ifEmpty {
            registry.allProviderIds.filter { registry.isProviderConfigured(it) }
        }

        for (providerId in orderedIds) {
            val provider = registry.getProvider(providerId) ?: continue
            val selectedModel = provider.supportedModels.firstOrNull()?.id ?: continue

            try {
                val accumulated = StringBuilder()
                provider.streamRewrite(input, tone, customPrompt, selectedModel)
                    .collect { token ->
                        accumulated.append(token)
                        emit(RewriteState.Streaming(accumulated.toString()))
                    }

                // Parse final result
                val parsed = ResponseParser.parse(accumulated.toString())
                val emojis = try {
                    provider.suggestEmojis(input, tone).getOrDefault(emptyList())
                } catch (_: Exception) {
                    getDefaultEmojis(tone)
                }

                emit(RewriteState.Success(
                    RewriteResult(
                        variants = parsed.variants,
                        emojis = emojis.take(5).ifEmpty { parsed.emojis },
                        tone = tone,
                        providerId = providerId
                    )
                ))
                return@flow // Success — stop trying providers
            } catch (_: Exception) {
                // Try next provider
            }
        }

        emit(RewriteState.Error("All providers failed"))
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
        Tone.CUSTOM -> listOf("✨", "💫", "🎨", "🎯", "💭")
    }
}
