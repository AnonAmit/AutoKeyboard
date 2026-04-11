package com.autokeyboard.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autokeyboard.data.local.PreferencesManager
import com.autokeyboard.data.local.SecureStorage
import com.autokeyboard.data.provider.ProviderRegistry
import com.autokeyboard.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val secureStorage: SecureStorage,
    private val providerRegistry: ProviderRegistry
) : ViewModel() {

    val theme = preferencesManager.theme.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.DARK
    )

    val hapticEnabled = preferencesManager.hapticEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val anonymousStats = preferencesManager.anonymousStats.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val providerInfoList = MutableStateFlow(providerRegistry.getProviderInfoList())

    // ═══ API Key Management ═══

    fun saveApiKey(providerId: String, key: String) {
        secureStorage.saveApiKey(providerId, key)
        providerRegistry.invalidateProvider(providerId)
        providerInfoList.value = providerRegistry.getProviderInfoList()
    }

    fun deleteApiKey(providerId: String) {
        secureStorage.deleteApiKey(providerId)
        providerRegistry.invalidateProvider(providerId)
        providerInfoList.value = providerRegistry.getProviderInfoList()
    }

    fun hasApiKey(providerId: String): Boolean = secureStorage.hasApiKey(providerId)

    fun getApiKeyMasked(providerId: String): String {
        val key = secureStorage.getApiKey(providerId) ?: return ""
        if (key.length <= 8) return "••••••••"
        return key.take(4) + "••••" + key.takeLast(4)
    }

    // ═══ Theme ═══

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { preferencesManager.setTheme(theme) }
    }

    // ═══ Privacy ═══

    fun setAnonymousStats(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setAnonymousStats(enabled) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setHapticEnabled(enabled) }
    }

    // ═══ Validation ═══

    private val _validationState = MutableStateFlow<Map<String, Boolean?>>(emptyMap())
    val validationState = _validationState.asStateFlow()

    fun validateApiKey(providerId: String, key: String) {
        viewModelScope.launch {
            _validationState.update { it + (providerId to null) } // loading
            val provider = providerRegistry.getProvider(providerId)
            val isValid = provider?.validateApiKey(key) ?: false
            _validationState.update { it + (providerId to isValid) }
        }
    }
}
