package com.autokeyboard.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.autokeyboard.ui.keyboard.KeyboardScreen
import com.autokeyboard.ui.theme.AutoKeyboardTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Core InputMethodService — the entry point for the AutoKeyboard IME.
 *
 * CRITICAL NOTES:
 * - InputMethodService runs in its own process
 * - NEVER store text from inputConnection anywhere
 * - ComposeView requires manual lifecycle owner setup
 */
@AndroidEntryPoint
class KeyboardService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, androidx.savedstate.SavedStateRegistryOwner {

    @Inject lateinit var viewModel: KeyboardViewModel

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = androidx.savedstate.SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val savedStateRegistry: androidx.savedstate.SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@KeyboardService)
            setViewTreeSavedStateRegistryOwner(this@KeyboardService)
            setViewTreeViewModelStoreOwner(this@KeyboardService)

            setContent {
                AutoKeyboardTheme {
                    KeyboardScreen(
                        onKeyPress = { char -> commitText(char) },
                        onBackspace = { handleBackspace() },
                        onEnter = { handleEnter() },
                        onImprove = { triggerRewrite() },
                        onInsertRewrite = { text -> insertRewrite(text) },
                        viewModel = viewModel
                    )
                }
            }
        }
        
        // CRITICAL: WindowRecomposer in Compose 1.3+ looks for LifecycleOwner on the window's decor view
        // once attached to the window hierarchy. Setting it on just ComposeView fails at onAttachToWindow.
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
        }

        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Detect current app for per-app tone memory
        info?.packageName?.let { packageName ->
            viewModel.onAppChanged(packageName)
        }
    }

    /**
     * Extracts current text from the active input field (up to 500 chars).
     * Text is NEVER stored — used only for the single rewrite call.
     */
    fun getCurrentText(): String {
        val ic = currentInputConnection ?: return ""
        // Get text before cursor
        val beforeCursor = ic.getTextBeforeCursor(500, 0)?.toString() ?: ""
        // Get selected text
        val selected = ic.getSelectedText(0)?.toString() ?: ""
        // Get text after cursor
        val afterCursor = ic.getTextAfterCursor(500 - beforeCursor.length, 0)?.toString() ?: ""

        return if (selected.isNotEmpty()) {
            selected // If text is selected, rewrite only the selection
        } else {
            (beforeCursor + afterCursor).take(500)
        }
    }

    /**
     * Replaces text in the active field with the rewritten version.
     * If text was selected, replaces only the selection.
     * Otherwise replaces all text in the field.
     */
    fun insertRewrite(text: String) {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)?.toString() ?: ""

        if (selected.isNotEmpty()) {
            // Replace selection
            ic.commitText(text, 1)
        } else {
            // Select all and replace
            ic.performContextMenuAction(android.R.id.selectAll)
            ic.commitText(text, 1)
        }
    }

    private fun commitText(char: String) {
        currentInputConnection?.commitText(char, 1)
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (selected != null && selected.isNotEmpty()) {
            ic.commitText("", 1) // Delete selection
        } else {
            ic.deleteSurroundingText(1, 0) // Delete char before cursor
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo

        // Check if the editor expects a specific action (Send, Search, etc.)
        val actionId = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: 0
        if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(actionId)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun triggerRewrite() {
        val text = getCurrentText()
        if (text.isNotBlank()) {
            viewModel.performRewrite(text)
        }
    }

    override fun onDestroy() {
        viewModel.clear()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}
