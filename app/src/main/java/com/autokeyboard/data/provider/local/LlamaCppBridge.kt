package com.autokeyboard.data.provider.local

/**
 * JNI bridge to llama.cpp native library for on-device inference.
 * Wraps native llama_context for Qwen and Llama model families.
 *
 * IMPORTANT: Requires libllama.so compiled for ARM64 in jniLibs/.
 * Native crashes are wrapped in try-catch with graceful fallback.
 */
class LlamaCppBridge {

    private var contextPtr: Long = 0L
    private var isLoaded = false

    companion object {
        init {
            try {
                System.loadLibrary("llama")
            } catch (e: UnsatisfiedLinkError) {
                // Library not available — local inference will be disabled
            }
        }
    }

    /**
     * Loads a GGUF model file into memory.
     * @param modelPath Absolute path to the .bin/.gguf model file
     * @param contextLength Maximum context window size
     * @param threads Number of CPU threads to use (1-8)
     * @return true if model loaded successfully
     */
    fun loadModel(modelPath: String, contextLength: Int = 2048, threads: Int = 4): Boolean {
        return try {
            contextPtr = nativeLoadModel(modelPath, contextLength, threads)
            isLoaded = contextPtr != 0L
            isLoaded
        } catch (e: Exception) {
            isLoaded = false
            false
        }
    }

    /**
     * Generates text completion from the loaded model.
     * @param prompt The full prompt (system + user combined)
     * @param maxTokens Maximum tokens to generate
     * @param temperature Sampling temperature (0.0 - 2.0)
     * @return Generated text or null on failure
     */
    fun generate(prompt: String, maxTokens: Int = 512, temperature: Float = 0.7f): String? {
        if (!isLoaded) return null
        return try {
            nativeGenerate(contextPtr, prompt, maxTokens, temperature)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates text token by token, calling the callback for each token.
     * @param prompt The full prompt
     * @param maxTokens Maximum tokens to generate
     * @param temperature Sampling temperature
     * @param onToken Called with each generated token string
     */
    fun generateStreaming(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f,
        onToken: (String) -> Unit
    ) {
        if (!isLoaded) return
        try {
            nativeGenerateStreaming(contextPtr, prompt, maxTokens, temperature, onToken)
        } catch (_: Exception) { }
    }

    /**
     * Unloads the model and frees native memory.
     */
    fun unload() {
        if (isLoaded) {
            try {
                nativeFreeModel(contextPtr)
            } catch (_: Exception) { }
            contextPtr = 0L
            isLoaded = false
        }
    }

    fun isModelLoaded(): Boolean = isLoaded

    // ═══ Native method declarations ═══
    private external fun nativeLoadModel(path: String, contextLength: Int, threads: Int): Long
    private external fun nativeGenerate(ctx: Long, prompt: String, maxTokens: Int, temperature: Float): String?
    private external fun nativeGenerateStreaming(ctx: Long, prompt: String, maxTokens: Int, temperature: Float, callback: (String) -> Unit)
    private external fun nativeFreeModel(ctx: Long)
}
