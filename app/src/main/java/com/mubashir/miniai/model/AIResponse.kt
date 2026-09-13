package com.mubashir.miniai.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AIResponse(
    @Json(name = "message")
    val message: String,
    @Json(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "tokens_generated")
    val tokensGenerated: Int = 0,
    @Json(name = "inference_time_ms")
    val inferenceTimeMs: Long = 0
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role")
    val role: String, // "user" or "assistant"
    @Json(name = "content")
    val content: String,
    @Json(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class ModelInfo(
    val name: String,
    val path: String,
    val size: Long,
    val contextSize: Int = 2048,
    val parameters: String = "",
    val quantization: String = "",
    val isLoaded: Boolean = false,
    val loadedAt: Long = 0
)

data class InferenceConfig(
    val maxTokens: Int = 256,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val repeatPenalty: Float = 1.1f,
    val numThreads: Int = 4,
    val useMetal: Boolean = false
)

data class InferenceResult(
    val output: String,
    val tokensGenerated: Int,
    val inferenceTimeMs: Long,
    val stopped: Boolean = false,
    val stopReason: String = ""
)
