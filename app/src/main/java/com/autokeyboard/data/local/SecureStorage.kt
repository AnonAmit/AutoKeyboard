package com.autokeyboard.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Securely stores API keys using EncryptedSharedPreferences backed by AndroidKeyStore.
 * Keys are never written to logs, DataStore, or any unencrypted storage.
 */
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "autokeyboard_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ═══ API Key Management ═══

    fun saveApiKey(providerId: String, apiKey: String) {
        securePrefs.edit().putString("api_key_$providerId", apiKey).apply()
    }

    fun getApiKey(providerId: String): String? {
        return securePrefs.getString("api_key_$providerId", null)
    }

    fun deleteApiKey(providerId: String) {
        securePrefs.edit().remove("api_key_$providerId").apply()
    }

    fun hasApiKey(providerId: String): Boolean {
        return securePrefs.contains("api_key_$providerId")
    }

    // ═══ Custom Base URL Management ═══

    fun saveBaseUrl(providerId: String, url: String) {
        securePrefs.edit().putString("base_url_$providerId", url).apply()
    }

    fun getBaseUrl(providerId: String): String? {
        return securePrefs.getString("base_url_$providerId", null)
    }

    fun deleteBaseUrl(providerId: String) {
        securePrefs.edit().remove("base_url_$providerId").apply()
    }

    // ═══ Clear All ═══

    fun clearAll() {
        securePrefs.edit().clear().apply()
    }
}
