package com.mubashir.miniai.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mubashir.miniai.data.entity.ModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {
    @Insert
    suspend fun insertModel(model: ModelEntity)

    @Query("SELECT * FROM models ORDER BY last_used DESC")
    fun getAllModels(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE id = :modelId")
    suspend fun getModelById(modelId: String): ModelEntity?

    @Query("SELECT * FROM models ORDER BY added_at DESC LIMIT 1")
    suspend fun getLatestModel(): ModelEntity?

    @Update
    suspend fun updateModel(model: ModelEntity)

    @Delete
    suspend fun deleteModel(model: ModelEntity)

    @Query("DELETE FROM models WHERE id = :modelId")
    suspend fun deleteModelById(modelId: String)

    @Query("DELETE FROM models")
    suspend fun deleteAllModels()

    @Query("SELECT COUNT(*) FROM models")
    fun getModelCount(): Flow<Int>
}
