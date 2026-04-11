package com.autokeyboard.data.provider

import com.autokeyboard.data.local.SecureStorage
import com.autokeyboard.data.provider.cloud.*
import com.autokeyboard.data.provider.local.*
import com.autokeyboard.domain.provider.AIProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central registry of all AI providers (cloud + local).
 * Lazily instantiates providers when API keys are available.
 */
@Singleton
class ProviderRegistry @Inject constructor(
    private val secureStorage: SecureStorage,
    private val localModelManager: LocalModelManager
) {
    private val providers = mutableMapOf<String, AIProvider>()

    /** All known provider IDs in display order */
    val allProviderIds = listOf(
        "openai", "gemini", "claude", "mistral", "groq",
        "cohere", "openrouter", "deepseek", "together", "custom",
        "gemma_local", "phi_local", "qwen_local", "llama_local", "mlc_local"
    )

    val cloudProviderIds = allProviderIds.take(10)
    val localProviderIds = allProviderIds.drop(10)

    /**
     * Gets or creates a provider by ID.
     * Cloud providers require an API key to be configured.
     */
    fun getProvider(id: String): AIProvider? {
        providers[id]?.let { return it }

        val provider = when (id) {
            "openai" -> secureStorage.getApiKey("openai")?.let {
                OpenAIProvider(it, secureStorage.getBaseUrl("openai"))
            }
            "gemini" -> secureStorage.getApiKey("gemini")?.let { GeminiProvider(it) }
            "claude" -> secureStorage.getApiKey("claude")?.let { ClaudeProvider(it) }
            "mistral" -> secureStorage.getApiKey("mistral")?.let { MistralProvider(it) }
            "groq" -> secureStorage.getApiKey("groq")?.let { GroqProvider(it) }
            "cohere" -> secureStorage.getApiKey("cohere")?.let { CohereProvider(it) }
            "openrouter" -> secureStorage.getApiKey("openrouter")?.let { OpenRouterProvider(it) }
            "deepseek" -> secureStorage.getApiKey("deepseek")?.let { DeepSeekProvider(it) }
            "together" -> secureStorage.getApiKey("together")?.let { TogetherProvider(it) }
            "custom" -> {
                val baseUrl = secureStorage.getBaseUrl("custom") ?: return null
                val apiKey = secureStorage.getApiKey("custom") ?: ""
                CustomProvider(apiKey, baseUrl, "")
            }
            "gemma_local" -> GemmaProvider(localModelManager)
            "phi_local" -> PhiProvider(localModelManager)
            "qwen_local" -> QwenProvider(localModelManager)
            "llama_local" -> LlamaLocalProvider(localModelManager)
            "mlc_local" -> MlcProvider(localModelManager)
            else -> null
        }

        if (provider != null) {
            providers[id] = provider
        }
        return provider
    }

    /**
     * Returns all currently configured (API key set) cloud providers.
     */
    fun getConfiguredCloudProviders(): List<AIProvider> {
        return cloudProviderIds.mapNotNull { getProvider(it) }.filter { !it.isLocal }
    }

    /**
     * Returns all local providers (always available, no API key needed).
     */
    fun getLocalProviders(): List<AIProvider> {
        return localProviderIds.mapNotNull { getProvider(it) }
    }

    /**
     * Checks if a provider has been configured with an API key.
     */
    fun isProviderConfigured(id: String): Boolean {
        if (localProviderIds.contains(id)) return true
        return secureStorage.hasApiKey(id)
    }

    /**
     * Invalidates a cached provider instance (e.g., after API key change).
     */
    fun invalidateProvider(id: String) {
        providers.remove(id)
    }

    /**
     * Provider display info for settings UI.
     */
    data class ProviderInfo(
        val id: String,
        val displayName: String,
        val description: String,
        val iconName: String,
        val isLocal: Boolean,
        val isConfigured: Boolean
    )

    fun getProviderInfoList(): List<ProviderInfo> {
        return allProviderIds.map { id ->
            val provider = getProvider(id)
            ProviderInfo(
                id = id,
                displayName = provider?.displayName ?: id.replaceFirstChar { it.uppercase() },
                description = provider?.description ?: "",
                iconName = provider?.iconName ?: "cloud",
                isLocal = localProviderIds.contains(id),
                isConfigured = isProviderConfigured(id)
            )
        }
    }
}
