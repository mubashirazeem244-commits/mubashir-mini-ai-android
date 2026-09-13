package com.mubashir.miniai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubashir.miniai.data.entity.ModelEntity
import com.mubashir.miniai.data.repository.ModelRepository
import com.mubashir.miniai.llm.GGUFModelLoader
import com.mubashir.miniai.model.GGUFModel
import com.mubashir.miniai.model.InferenceConfig
import com.mubashir.miniai.model.InferenceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val modelLoader: GGUFModelLoader,
    private val modelRepository: ModelRepository
) : ViewModel() {

    private val _availableModels = MutableStateFlow<List<GGUFModel>>(emptyList())
    val availableModels: StateFlow<List<GGUFModel>> = _availableModels.asStateFlow()

    private val _currentModel = MutableStateFlow<GGUFModel?>(null)
    val currentModel: StateFlow<GGUFModel?> = _currentModel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inferenceResult = MutableStateFlow<InferenceResult?>(null)
    val inferenceResult: StateFlow<InferenceResult?> = _inferenceResult.asStateFlow()

    private val _uiState = MutableStateFlow<AIUiState>(AIUiState.Idle)
    val uiState: StateFlow<AIUiState> = _uiState.asStateFlow()

    private val _inferenceConfig = MutableStateFlow(InferenceConfig())
    val inferenceConfig: StateFlow<InferenceConfig> = _inferenceConfig.asStateFlow()

    init {
        loadAvailableModels()
    }

    fun loadAvailableModels() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val models = modelLoader.listAvailableModels()
                _availableModels.value = models
                Timber.d("Loaded ${models.size} available models")
                _uiState.value = AIUiState.ModelsLoaded(models.size)
            } catch (e: Exception) {
                Timber.e(e, "Error loading models")
                _uiState.value = AIUiState.Error("Failed to load models: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadModel(model: GGUFModel) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _uiState.value = AIUiState.LoadingModel(model.name)
                
                val success = modelLoader.loadModel(model, _inferenceConfig.value)
                if (success) {
                    _currentModel.value = model
                    _uiState.value = AIUiState.ModelLoaded(model.name)
                    Timber.d("Model loaded: ${model.name}")
                    
                    // Save to database
                    val modelEntity = ModelEntity(
                        id = model.name,
                        name = model.name,
                        path = model.file.absolutePath,
                        sizeBytes = model.size,
                        contextSize = model.contextSize,
                        quantization = model.quantizationType,
                        lastUsed = System.currentTimeMillis()
                    )
                    modelRepository.updateModel(modelEntity)
                } else {
                    _uiState.value = AIUiState.Error("Failed to load model")
                    Timber.e("Failed to load model: ${model.name}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading model")
                _uiState.value = AIUiState.Error("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun runInference(prompt: String) {
        if (!modelLoader.isModelLoaded()) {
            _uiState.value = AIUiState.Error("No model loaded")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _uiState.value = AIUiState.Inferencing(prompt)
                
                val result = modelLoader.runInference(prompt, _inferenceConfig.value)
                _inferenceResult.value = result
                _uiState.value = AIUiState.InferenceComplete(result)
                
                Timber.d("Inference complete: ${result.tokensGenerated} tokens in ${result.inferenceTimeMs}ms")
            } catch (e: Exception) {
                Timber.e(e, "Inference error")
                _uiState.value = AIUiState.Error("Inference failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateInferenceConfig(config: InferenceConfig) {
        _inferenceConfig.value = config
        Timber.d("Inference config updated: temp=${config.temperature}, topP=${config.topP}")
    }

    fun unloadModel() {
        viewModelScope.launch {
            try {
                modelLoader.unloadModel()
                _currentModel.value = null
                _uiState.value = AIUiState.Idle
                Timber.d("Model unloaded")
            } catch (e: Exception) {
                Timber.e(e, "Error unloading model")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        modelLoader.cleanup()
    }
}

sealed class AIUiState {
    object Idle : AIUiState()
    data class ModelsLoaded(val count: Int) : AIUiState()
    data class LoadingModel(val modelName: String) : AIUiState()
    data class ModelLoaded(val modelName: String) : AIUiState()
    data class Inferencing(val prompt: String) : AIUiState()
    data class InferenceComplete(val result: InferenceResult) : AIUiState()
    data class Error(val message: String) : AIUiState()
}
