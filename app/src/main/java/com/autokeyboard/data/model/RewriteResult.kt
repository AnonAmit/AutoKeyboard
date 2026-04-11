package com.autokeyboard.data.model

/**
 * Result of an AI rewrite operation.
 * Contains multiple text variants + contextual emojis.
 */
data class RewriteResult(
    val variants: List<String>,
    val emojis: List<String>,
    val tone: Tone,
    val providerId: String,
    val latencyMs: Long = 0
)

/**
 * Streaming state for progressive rewrite display.
 */
sealed class RewriteState {
    data object Idle : RewriteState()
    data object Loading : RewriteState()
    data class Streaming(val partialText: String, val variantIndex: Int = 0) : RewriteState()
    data class Success(val result: RewriteResult) : RewriteState()
    data class Error(val message: String, val canRetry: Boolean = true) : RewriteState()
}
