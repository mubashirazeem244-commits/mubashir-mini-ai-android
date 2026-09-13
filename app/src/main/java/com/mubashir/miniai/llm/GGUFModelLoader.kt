package com.mubashir.miniai.llm

import android.content.Context
import com.mubashir.miniai.model.GGUFModel
import com.mubashir.miniai.model.InferenceConfig
import com.mubashir.miniai.model.InferenceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import kotlin.math.min

/**
 * Loader and manager for GGUF format models
 * Provides methods to load, list, and manage GGUF models on device
 */
class GGUFModelLoader(private val context: Context) {

    private val llamaCpp = LlamaCppInterface(context)
    private var currentModel: GGUFModel? = null
    private val lock = Any()

    companion object {
        private const val MODELS_DIR = "AI_Models"
        private const val MIN_MODEL_SIZE = 50 * 1024 * 1024L // 50MB minimum
    }

    /**
     * Get the models directory path
     */
    fun getModelsDirectory(): File {
        val externalFilesDir = context.getExternalFilesDir(null)
            ?: throw IllegalStateException("External files directory not available")
        return File(externalFilesDir, MODELS_DIR).apply { mkdirs() }
    }

    /**
     * List all available GGUF models in the models directory
     */
    suspend fun listAvailableModels(): List<GGUFModel> = withContext(Dispatchers.IO) {
        try {
            val modelsDir = getModelsDirectory()
            if (!modelsDir.exists()) {
                Timber.d("Models directory does not exist: ${modelsDir.path}")
                return@withContext emptyList()
            }

            val models = modelsDir.listFiles { file ->
                file.isFile && file.extension == "gguf" && file.length() >= MIN_MODEL_SIZE
            }?.map { file ->
                GGUFModel(
                    file = file,
                    name = file.nameWithoutExtension,
                    size = file.length(),
                    contextSize = inferContextSize(file.name)
                )
            } ?: emptyList()

            Timber.d("Found ${models.size} GGUF models")
            models.sortedByDescending { it.size }
        } catch (e: Exception) {
            Timber.e(e, "Error listing models")
            emptyList()
        }
    }

    /**
     * Load a GGUF model and prepare for inference
     * @param model The GGUF model to load
     * @param config Inference configuration
     * @return true if successful
     */
    suspend fun loadModel(model: GGUFModel, config: InferenceConfig = InferenceConfig()): Boolean =
        withContext(Dispatchers.Default) {
            synchronized(lock) {
                return@withContext try {
                    Timber.d("Loading model: ${model.name} from ${model.file.path}")
                    Timber.d("Model size: ${model.size / (1024 * 1024)}MB, Quantization: ${model.quantizationType}")

                    // Unload previous model if any
                    if (currentModel != null) {
                        unloadModel()
                    }

                    val success = llamaCpp.loadModel(model.file.absolutePath, config)
                    if (success) {
                        currentModel = model
                        Timber.d("Model loaded successfully")
                    } else {
                        Timber.e("Failed to load model")
                    }
                    success
                } catch (e: Exception) {
                    Timber.e(e, "Error loading model")
                    false
                }
            }
        }

    /**
     * Run inference on the loaded model
     * @param prompt Input text prompt
     * @param config Inference configuration
     * @return InferenceResult containing generated text
     */
    suspend fun runInference(prompt: String, config: InferenceConfig = InferenceConfig()): InferenceResult =
        withContext(Dispatchers.Default) {
            synchronized(lock) {
                if (!llamaCpp.isModelLoaded()) {
                    Timber.w("No model loaded for inference")
                    return@withContext InferenceResult(
                        output = "Error: No model loaded",
                        tokensGenerated = 0,
                        inferenceTimeMs = 0
                    )
                }

                return@withContext try {
                    llamaCpp.inference(prompt, config)
                } catch (e: Exception) {
                    Timber.e(e, "Inference failed")
                    InferenceResult(
                        output = "Error: ${e.localizedMessage}",
                        tokensGenerated = 0,
                        inferenceTimeMs = 0
                    )
                }
            }
        }

    /**
     * Tokenize text using the loaded model
     * @param text Text to tokenize
     * @return List of token IDs
     */
    suspend fun tokenize(text: String): List<Int> = withContext(Dispatchers.Default) {
        synchronized(lock) {
            if (!llamaCpp.isModelLoaded()) {
                return@withContext emptyList()
            }
            return@withContext llamaCpp.tokenize(text)
        }
    }

    /**
     * Get information about the loaded model
     * @return Model information map
     */
    fun getLoadedModelInfo(): Map<String, String> {
        return if (llamaCpp.isModelLoaded()) {
            llamaCpp.getModelInfo()
        } else {
            emptyMap()
        }
    }

    /**
     * Get the currently loaded model
     */
    fun getCurrentModel(): GGUFModel? = currentModel

    /**
     * Unload the current model
     */
    fun unloadModel() = synchronized(lock) {
        try {
            llamaCpp.unloadModel()
            currentModel = null
            Timber.d("Model unloaded")
        } catch (e: Exception) {
            Timber.e(e, "Error unloading model")
        }
    }

    /**
     * Check if a model is currently loaded
     */
    fun isModelLoaded(): Boolean = synchronized(lock) {
        return llamaCpp.isModelLoaded()
    }

    /**
     * Delete a model file from storage
     * @param model The model to delete
     */
    suspend fun deleteModel(model: GGUFModel): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (currentModel?.file?.absolutePath == model.file.absolutePath) {
                unloadModel()
            }
            model.file.delete().also {
                if (it) Timber.d("Model deleted: ${model.name}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting model")
            false
        }
    }

    /**
     * Get available storage space
     */
    fun getAvailableStorageSpace(): Long {
        return try {
            val stat = android.os.StatFs(getModelsDirectory().path)
            stat.availableBytes
        } catch (e: Exception) {
            Timber.e(e, "Error getting storage space")
            0L
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        unloadModel()
    }

    private fun inferContextSize(filename: String): Int {
        return when {
            filename.contains("7b") -> 2048
            filename.contains("13b") -> 4096
            filename.contains("33b") -> 4096
            filename.contains("65b") -> 8192
            else -> 2048
        }
    }
}
