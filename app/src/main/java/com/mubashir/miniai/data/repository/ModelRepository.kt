package com.mubashir.miniai.data.repository

import com.mubashir.miniai.data.dao.ModelDao
import com.mubashir.miniai.data.entity.ModelEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ModelRepository(private val modelDao: ModelDao) {

    fun getAllModels(): Flow<List<ModelEntity>> {
        return modelDao.getAllModels()
    }

    suspend fun getModelById(modelId: String): ModelEntity? {
        return try {
            modelDao.getModelById(modelId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting model by id")
            null
        }
    }

    suspend fun getLatestModel(): ModelEntity? {
        return try {
            modelDao.getLatestModel()
        } catch (e: Exception) {
            Timber.e(e, "Error getting latest model")
            null
        }
    }

    suspend fun addModel(model: ModelEntity) {
        try {
            modelDao.insertModel(model)
        } catch (e: Exception) {
            Timber.e(e, "Error adding model")
        }
    }

    suspend fun updateModel(model: ModelEntity) {
        try {
            modelDao.updateModel(model)
        } catch (e: Exception) {
            Timber.e(e, "Error updating model")
        }
    }

    suspend fun deleteModel(model: ModelEntity) {
        try {
            modelDao.deleteModel(model)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting model")
        }
    }

    suspend fun deleteModelById(modelId: String) {
        try {
            modelDao.deleteModelById(modelId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting model by id")
        }
    }

    suspend fun deleteAllModels() {
        try {
            modelDao.deleteAllModels()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting all models")
        }
    }

    fun getModelCount(): Flow<Int> {
        return modelDao.getModelCount()
    }
}
