package com.mubashir.miniai.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mubashir.miniai.model.GGUFModel
import com.mubashir.miniai.ui.viewmodel.AIViewModel
import com.mubashir.miniai.ui.viewmodel.AIUiState

@Composable
fun ModelManagerScreen(aiViewModel: AIViewModel) {
    val availableModels by aiViewModel.availableModels.collectAsState()
    val currentModel by aiViewModel.currentModel.collectAsState()
    val isLoading by aiViewModel.isLoading.collectAsState()
    val uiState by aiViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Model Manager",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${availableModels.size} model(s) available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Status messages
        when (uiState) {
            is AIUiState.ModelLoaded -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓ ${(uiState as AIUiState.ModelLoaded).modelName} loaded",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            is AIUiState.Error -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✗ ${(uiState as AIUiState.Error).message}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            else -> {}
        }

        // Currently loaded model info
        if (currentModel != null) {
            CurrentModelCard(currentModel!!, onUnload = { aiViewModel.unloadModel() })
        }

        // Models list
        if (availableModels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No GGUF models found.\nPlace .gguf files in: /storage/emulated/0/Android/data/com.mubashir.miniai/files/AI_Models/",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableModels) { model ->
                    ModelCard(
                        model = model,
                        isLoaded = currentModel?.file?.absolutePath == model.file.absolutePath,
                        isLoading = isLoading,
                        onLoad = { aiViewModel.loadModel(model) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelCard(
    model: GGUFModel,
    isLoaded: Boolean,
    isLoading: Boolean,
    onLoad: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = if (isLoaded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isLoaded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${model.size / (1024 * 1024)}MB | ${model.quantizationType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isLoaded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isLoaded) {
                    Text(
                        text = "✓ LOADED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Green,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    Button(
                        onClick = onLoad,
                        enabled = !isLoading,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Load")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentModelCard(
    model: GGUFModel,
    onUnload: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Currently Loaded",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${model.size / (1024 * 1024)}MB | Context: ${model.contextSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Button(
                    onClick = onUnload,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Unload")
                }
            }
        }
    }
}
