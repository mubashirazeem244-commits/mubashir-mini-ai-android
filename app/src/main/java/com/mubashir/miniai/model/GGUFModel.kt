package com.mubashir.miniai.model

import java.io.File

data class GGUFModel(
    val file: File,
    val name: String = file.nameWithoutExtension,
    val size: Long = file.length(),
    val contextSize: Int = 2048,
    val quantizationType: String = extractQuantizationType(file.name)
) {
    companion object {
        fun extractQuantizationType(filename: String): String {
            return when {
                filename.contains("Q4_0") -> "Q4_0"
                filename.contains("Q4_1") -> "Q4_1"
                filename.contains("Q5_0") -> "Q5_0"
                filename.contains("Q5_1") -> "Q5_1"
                filename.contains("Q8_0") -> "Q8_0"
                filename.contains("F16") -> "F16"
                filename.contains("F32") -> "F32"
                else -> "Unknown"
            }
        }
    }
}
