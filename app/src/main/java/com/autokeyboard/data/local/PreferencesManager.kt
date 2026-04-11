package com.autokeyboard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.autokeyboard.data.model.Tone
import com.autokeyboard.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "autokeyboard_prefs")

/**
 * Manages all user preferences using Jetpack DataStore.
 * NEVER stores message content — privacy enforced by design.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ═══ Keys ═══
    private object Keys {
        val DEFAULT_TONE = stringPreferencesKey("default_tone")
        val CUSTOM_PROMPT = stringPreferencesKey("custom_prompt")
        val VARIANT_COUNT = intPreferencesKey("variant_count")
        val STREAMING_ENABLED = booleanPreferencesKey("streaming_enabled")
        val AUTO_REWRITE_ON_PASTE = booleanPreferencesKey("auto_rewrite_on_paste")
        val THEME = stringPreferencesKey("theme")
        val KEYBOARD_HEIGHT_DP = intPreferencesKey("keyboard_height_dp")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val HAPTIC_INTENSITY = floatPreferencesKey("haptic_intensity")
        val SHOW_PROVIDER_ON_SPACEBAR = booleanPreferencesKey("show_provider_on_spacebar")
        val ANONYMOUS_STATS = booleanPreferencesKey("anonymous_stats")
        val PER_APP_TONES = stringPreferencesKey("per_app_tones") // JSON map
        val PROVIDER_PRIORITY = stringPreferencesKey("provider_priority") // JSON list
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    // ═══ Default Tone ═══
    val defaultTone: Flow<Tone> = dataStore.data.map { prefs ->
        Tone.fromName(prefs[Keys.DEFAULT_TONE] ?: Tone.PROFESSIONAL.name)
    }

    suspend fun setDefaultTone(tone: Tone) {
        dataStore.edit { it[Keys.DEFAULT_TONE] = tone.name }
    }

    // ═══ Custom Prompt ═══
    val customPrompt: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_PROMPT] ?: ""
    }

    suspend fun setCustomPrompt(prompt: String) {
        dataStore.edit { it[Keys.CUSTOM_PROMPT] = prompt }
    }

    // ═══ Variant Count ═══
    val variantCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.VARIANT_COUNT] ?: 2
    }

    suspend fun setVariantCount(count: Int) {
        dataStore.edit { it[Keys.VARIANT_COUNT] = count.coerceIn(2, 3) }
    }

    // ═══ Streaming ═══
    val streamingEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.STREAMING_ENABLED] ?: true
    }

    suspend fun setStreamingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.STREAMING_ENABLED] = enabled }
    }

    // ═══ Auto Rewrite on Paste ═══
    val autoRewriteOnPaste: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.AUTO_REWRITE_ON_PASTE] ?: false
    }

    suspend fun setAutoRewriteOnPaste(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_REWRITE_ON_PASTE] = enabled }
    }

    // ═══ Theme ═══
    val theme: Flow<AppTheme> = dataStore.data.map { prefs ->
        try {
            AppTheme.valueOf(prefs[Keys.THEME] ?: AppTheme.DARK.name)
        } catch (_: Exception) {
            AppTheme.DARK
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    // ═══ Keyboard Height ═══
    val keyboardHeightDp: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.KEYBOARD_HEIGHT_DP] ?: 280
    }

    suspend fun setKeyboardHeightDp(height: Int) {
        dataStore.edit { it[Keys.KEYBOARD_HEIGHT_DP] = height.coerceIn(240, 320) }
    }

    // ═══ Haptic Feedback ═══
    val hapticEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.HAPTIC_ENABLED] ?: true
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    val hapticIntensity: Flow<Float> = dataStore.data.map { prefs ->
        prefs[Keys.HAPTIC_INTENSITY] ?: 0.5f
    }

    suspend fun setHapticIntensity(intensity: Float) {
        dataStore.edit { it[Keys.HAPTIC_INTENSITY] = intensity.coerceIn(0f, 1f) }
    }

    // ═══ Privacy ═══
    val showProviderOnSpacebar: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.SHOW_PROVIDER_ON_SPACEBAR] ?: true
    }

    suspend fun setShowProviderOnSpacebar(show: Boolean) {
        dataStore.edit { it[Keys.SHOW_PROVIDER_ON_SPACEBAR] = show }
    }

    val anonymousStats: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ANONYMOUS_STATS] ?: false
    }

    suspend fun setAnonymousStats(enabled: Boolean) {
        dataStore.edit { it[Keys.ANONYMOUS_STATS] = enabled }
    }

    // ═══ Per-App Tones ═══
    val perAppTones: Flow<Map<String, String>> = dataStore.data.map { prefs ->
        val json = prefs[Keys.PER_APP_TONES] ?: "{}"
        try {
            Json.decodeFromString<Map<String, String>>(json)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    suspend fun setToneForApp(packageName: String, tone: Tone) {
        dataStore.edit { prefs ->
            val current = try {
                Json.decodeFromString<Map<String, String>>(prefs[Keys.PER_APP_TONES] ?: "{}")
            } catch (_: Exception) {
                emptyMap()
            }
            val updated = current.toMutableMap().apply { put(packageName, tone.name) }
            prefs[Keys.PER_APP_TONES] = Json.encodeToString(updated)
        }
    }

    // ═══ Provider Priority ═══
    val providerPriority: Flow<List<String>> = dataStore.data.map { prefs ->
        val json = prefs[Keys.PROVIDER_PRIORITY] ?: "[]"
        try {
            Json.decodeFromString<List<String>>(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun setProviderPriority(orderedIds: List<String>) {
        dataStore.edit { it[Keys.PROVIDER_PRIORITY] = Json.encodeToString(orderedIds) }
    }

    // ═══ Onboarding ═══
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = true }
    }
}
