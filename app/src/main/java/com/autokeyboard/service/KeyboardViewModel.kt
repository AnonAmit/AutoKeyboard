package com.autokeyboard.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autokeyboard.data.local.PreferencesManager
import com.autokeyboard.data.model.RewriteState
import com.autokeyboard.data.model.Tone
import com.autokeyboard.data.provider.RewriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the keyboard. Manages UI state, tone selection,
 * and coordinates rewrite operations.
 */
@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val rewriteRepository: RewriteRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // ═══ Keyboard Mode ═══
    enum class KeyboardMode { TYPING, TONE_SELECT, REWRITE_RESULTS, EMOJI_STRIP }

    private val _keyboardMode = MutableStateFlow(KeyboardMode.TYPING)
    val keyboardMode: StateFlow<KeyboardMode> = _keyboardMode.asStateFlow()

    // ═══ Shift State ═══
    private val _isShifted = MutableStateFlow(false)
    val isShifted: StateFlow<Boolean> = _isShifted.asStateFlow()

    private val _isCapsLock = MutableStateFlow(false)
    val isCapsLock: StateFlow<Boolean> = _isCapsLock.asStateFlow()

    // ═══ Number/Symbol Mode ═══
    private val _isNumberMode = MutableStateFlow(false)
    val isNumberMode: StateFlow<Boolean> = _isNumberMode.asStateFlow()

    // ═══ Current Tone ═══
    private val _currentTone = MutableStateFlow(Tone.PROFESSIONAL)
    val currentTone: StateFlow<Tone> = _currentTone.asStateFlow()

    // ═══ Custom Prompt ═══
    private val _customPrompt = MutableStateFlow("")
    val customPrompt: StateFlow<String> = _customPrompt.asStateFlow()

    // ═══ Rewrite State ═══
    private val _rewriteState = MutableStateFlow<RewriteState>(RewriteState.Idle)
    val rewriteState: StateFlow<RewriteState> = _rewriteState.asStateFlow()

    // ═══ Current Input Text (transient, never persisted) ═══
    private var currentInputText = ""

    init {
        // Load saved preferences
        viewModelScope.launch {
            preferencesManager.defaultTone.collect { tone ->
                _currentTone.value = tone
            }
        }
        viewModelScope.launch {
            preferencesManager.customPrompt.collect { prompt ->
                _customPrompt.value = prompt
            }
        }
    }

    // ═══ Key Actions ═══

    fun onShiftToggle() {
        if (_isCapsLock.value) {
            _isCapsLock.value = false
            _isShifted.value = false
        } else if (_isShifted.value) {
            _isCapsLock.value = true
        } else {
            _isShifted.value = true
        }
    }

    fun onKeyTyped() {
        // Auto-unshift after typing (unless caps lock)
        if (_isShifted.value && !_isCapsLock.value) {
            _isShifted.value = false
        }
    }

    fun onNumberModeToggle() {
        _isNumberMode.value = !_isNumberMode.value
    }

    // ═══ Mode Switching ═══

    fun showToneSelector() {
        _keyboardMode.value = KeyboardMode.TONE_SELECT
    }

    fun showEmojiStrip() {
        _keyboardMode.value = KeyboardMode.EMOJI_STRIP
    }

    fun dismissOverlay() {
        _keyboardMode.value = KeyboardMode.TYPING
        _rewriteState.value = RewriteState.Idle
    }

    // ═══ Tone Selection ═══

    fun selectTone(tone: Tone) {
        _currentTone.value = tone
        viewModelScope.launch {
            preferencesManager.setDefaultTone(tone)
        }
    }

    fun updateCustomPrompt(prompt: String) {
        _customPrompt.value = prompt
        viewModelScope.launch {
            preferencesManager.setCustomPrompt(prompt)
        }
    }

    // ═══ Rewrite Operations ═══

    fun performRewrite(text: String) {
        currentInputText = text
        _rewriteState.value = RewriteState.Loading
        _keyboardMode.value = KeyboardMode.REWRITE_RESULTS

        viewModelScope.launch {
            val result = rewriteRepository.rewrite(
                input = text,
                tone = _currentTone.value,
                customPrompt = _customPrompt.value.takeIf { it.isNotBlank() },
                variantCount = 2
            )
            _rewriteState.value = result
        }
    }

    fun regenerate() {
        if (currentInputText.isNotBlank()) {
            performRewrite(currentInputText)
        }
    }

    // ═══ Per-App Context ═══

    fun onAppChanged(packageName: String) {
        viewModelScope.launch {
            preferencesManager.perAppTones.first().let { toneMap ->
                toneMap[packageName]?.let { toneName ->
                    _currentTone.value = Tone.fromName(toneName)
                }
            }
        }
    }
}
