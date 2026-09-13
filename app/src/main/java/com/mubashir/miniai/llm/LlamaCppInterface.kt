package com.mubashir.miniai.llm

import android.content.Context
import com.mubashir.miniai.model.InferenceConfig
import com.mubashir.miniai.model.InferenceResult
import timber.log.Timber
import java.io.File

/**
 * Interface for interacting with Llama.cpp C++ native library
 * This class bridges Kotlin to the JNI bindings for llama.cpp
 */
class LlamaCppInterface(private val context: Context) {

    private var modelPointer: Long = 0
    private var contextPointer: Long = 0
    private var isInitialized = false
    private val lock = Any()

    companion object {
        init {
            try {
                System.loadLibrary("llama")
                System.loadLibrary("ggml")
                Timber.d("Native libraries loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Timber.e(e, "Failed to load native libraries")
            }
        }
    }

    /**
     * Load a GGUF model file and initialize the inference context
     * @param modelPath Path to the GGUF model file
     * @param config Inference configuration
     * @return true if successful
     */
    fun loadModel(modelPath: String, config: InferenceConfig): Boolean = synchronized(lock) {
        return try {
            if (!File(modelPath).exists()) {
                Timber.e("Model file not found: $modelPath")
                return false
            }

            Timber.d("Loading model from: $modelPath")
            
            // Initialize model with context configuration
            val modelParams = createModelParams(modelPath, config)
            modelPointer = nativeLoadModel(modelPath, modelParams)
            
            if (modelPointer == 0L) {
                Timber.e("Failed to load model")
                return false
            }

            contextPointer = nativeCreateContext(modelPointer, config.maxTokens)
            if (contextPointer == 0L) {
                Timber.e("Failed to create inference context")
                return false
            }

            isInitialized = true
            Timber.d("Model loaded successfully with context")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error loading model")
            false
        }
    }

    /**
     * Run inference on the given prompt
     * @param prompt Input text prompt
     * @param config Inference configuration
     * @return InferenceResult containing generated text and timing info
     */
    fun inference(prompt: String, config: InferenceConfig): InferenceResult = synchronized(lock) {
        if (!isInitialized || modelPointer == 0L) {
            Timber.w("Model not initialized")
            return InferenceResult(
                output = "Error: Model not loaded",
                tokensGenerated = 0,
                inferenceTimeMs = 0
            )
        }

        return try {
            val startTime = System.currentTimeMillis()
            
            Timber.d("Running inference with prompt: $prompt")
            Timber.d("Config: temp=${config.temperature}, topP=${config.topP}, maxTokens=${config.maxTokens}")

            val result = nativeInference(
                modelPointer = modelPointer,
                contextPointer = contextPointer,
                prompt = prompt,
                maxTokens = config.maxTokens,
                temperature = config.temperature,
                topP = config.topP,
                topK = config.topK,
                repeatPenalty = config.repeatPenalty,
                numThreads = config.numThreads
            )

            val inferenceTimeMs = System.currentTimeMillis() - startTime
            
            Timber.d("Inference completed in ${inferenceTimeMs}ms")
            Timber.d("Generated text: $result")

            InferenceResult(
                output = result,
                tokensGenerated = estimateTokenCount(result),
                inferenceTimeMs = inferenceTimeMs,
                stopped = true
            )
        } catch (e: Exception) {
            Timber.e(e, "Inference error")
            InferenceResult(
                output = "Error: ${e.message}",
                tokensGenerated = 0,
                inferenceTimeMs = 0
            )
        }
    }

    /**
     * Tokenize input text
     * @param text Input text
     * @return List of token IDs
     */
    fun tokenize(text: String): List<Int> = synchronized(lock) {
        if (!isInitialized || contextPointer == 0L) {
            return emptyList()
        }
        return try {
            nativeTokenize(contextPointer, text)
        } catch (e: Exception) {
            Timber.e(e, "Tokenization error")
            emptyList()
        }
    }

    /**
     * Get model information and capabilities
     * @return Map of model properties
     */
    fun getModelInfo(): Map<String, String> = synchronized(lock) {
        if (!isInitialized || modelPointer == 0L) {
            return emptyMap()
        }
        return try {
            nativeGetModelInfo(modelPointer)
        } catch (e: Exception) {
            Timber.e(e, "Error getting model info")
            emptyMap()
        }
    }

    /**
     * Unload the model and free resources
     */
    fun unloadModel() = synchronized(lock) {
        try {
            if (contextPointer != 0L) {
                nativeFreeContext(contextPointer)
                contextPointer = 0
            }
            if (modelPointer != 0L) {
                nativeFreeModel(modelPointer)
                modelPointer = 0
            }
            isInitialized = false
            Timber.d("Model unloaded")
        } catch (e: Exception) {
            Timber.e(e, "Error unloading model")
        }
    }

    /**
     * Check if model is loaded
     */
    fun isModelLoaded(): Boolean = synchronized(lock) {
        return isInitialized && modelPointer != 0L && contextPointer != 0L
    }

    // Native methods (JNI)
    private external fun nativeLoadModel(modelPath: String, params: String): Long
    private external fun nativeCreateContext(modelPointer: Long, contextSize: Int): Long
    private external fun nativeInference(
        modelPointer: Long,
        contextPointer: Long,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        topP: Float,
        topK: Int,
        repeatPenalty: Float,
        numThreads: Int
    ): String
    private external fun nativeTokenize(contextPointer: Long, text: String): List<Int>
    private external fun nativeGetModelInfo(modelPointer: Long): Map<String, String>
    private external fun nativeFreeContext(contextPointer: Long)
    private external fun nativeFreeModel(modelPointer: Long)

    private fun createModelParams(modelPath: String, config: InferenceConfig): String {
        return """
            context_size=${config.maxTokens}
            num_threads=${config.numThreads}
            use_metal=${config.useMetal}
        """.trimIndent()
    }

    private fun estimateTokenCount(text: String): Int {
        // Rough estimate: 1 token ≈ 4 characters
        return (text.length / 4).coerceAtLeast(1)
    }
}
