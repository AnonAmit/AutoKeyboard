package com.autokeyboard.domain.provider

import com.autokeyboard.data.model.Model
import com.autokeyboard.data.model.Tone
import kotlinx.coroutines.flow.Flow

/**
 * Core interface that ALL AI providers must implement.
 * Both cloud and local providers share this contract.
 */
interface AIProvider {
    /** Unique identifier (e.g., "openai", "gemini", "gemma_local") */
    val id: String

    /** Display name shown in Settings UI */
    val displayName: String

    /** True for on-device providers (no network required) */
    val isLocal: Boolean

    /** Models the user can select from within this provider */
    val supportedModels: List<Model>

    /** Icon name for Material Symbols Outlined */
    val iconName: String

    /** Short description for settings cards */
    val description: String

    /**
     * Performs a full rewrite and returns all variants at once.
     * @param input The user's original text (max 500 chars)
     * @param tone Selected tone for rewriting
     * @param customPrompt Optional user-defined style instruction
     * @param model Selected model ID within this provider
     * @return List of rewritten text variants
     */
    suspend fun rewrite(
        input: String,
        tone: Tone,
        customPrompt: String?,
        model: String
    ): Result<List<String>>

    /**
     * Performs a streaming rewrite, emitting text progressively.
     * @return Flow of partial text as it's generated
     */
    fun streamRewrite(
        input: String,
        tone: Tone,
        customPrompt: String?,
        model: String
    ): Flow<String>

    /**
     * Suggests contextual emojis based on input and tone.
     * @return List of 5 emoji characters
     */
    suspend fun suggestEmojis(
        input: String,
        tone: Tone
    ): Result<List<String>>

    /**
     * Validates that an API key works for this provider.
     * For local providers, always returns true.
     */
    suspend fun validateApiKey(key: String): Boolean
}
