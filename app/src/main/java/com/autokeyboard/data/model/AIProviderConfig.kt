package com.autokeyboard.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a selectable AI model within a provider.
 */
@Serializable
data class Model(
    val id: String,
    val displayName: String,
    val description: String = "",
    val contextLength: Int = 4096,
    val supportsStreaming: Boolean = true
)

/**
 * Configuration for a cloud AI provider, persisted in DataStore.
 */
@Serializable
data class ProviderConfig(
    val providerId: String,
    val enabled: Boolean = false,
    val selectedModelId: String = "",
    val temperature: Float = 0.7f,
    val customBaseUrl: String? = null,
    val priority: Int = Int.MAX_VALUE // Lower = higher priority
)

/**
 * Runtime for local/on-device AI models.
 */
enum class LocalRuntime {
    MEDIAPIPE,
    ONNX,
    LLAMACPP,
    MLC
}

/**
 * Descriptor for a downloadable on-device AI model.
 */
@Serializable
data class LocalModel(
    val id: String,
    val displayName: String,
    val providerId: String,
    val sizeGB: Float,
    val ramRequiredMB: Int,
    val downloadUrl: String,
    val runtimeName: String, // LocalRuntime name
    val quantization: String // Q4, Q6, Q8, INT4
) {
    val runtime: LocalRuntime
        get() = LocalRuntime.valueOf(runtimeName)
}

/**
 * Download progress state for model downloads.
 */
data class DownloadProgress(
    val modelId: String,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val progressFraction: Float
        get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
}
