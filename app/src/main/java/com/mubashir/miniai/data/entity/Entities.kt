package com.mubashir.miniai.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "role")
    val role: String, // "user" or "assistant"
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "session_id")
    val sessionId: String = "",
    @ColumnInfo(name = "tokens_generated")
    val tokensGenerated: Int = 0,
    @ColumnInfo(name = "inference_time_ms")
    val inferenceTimeMs: Long = 0
)

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey
    val id: String = "",
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "path")
    val path: String,
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,
    @ColumnInfo(name = "context_size")
    val contextSize: Int = 2048,
    @ColumnInfo(name = "quantization")
    val quantization: String = "",
    @ColumnInfo(name = "parameters")
    val parameters: String = "",
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_used")
    val lastUsed: Long = 0
)
