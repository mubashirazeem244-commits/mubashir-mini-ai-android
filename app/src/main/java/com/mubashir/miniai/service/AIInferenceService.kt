package com.mubashir.miniai.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.mubashir.miniai.llm.GGUFModelLoader
import com.mubashir.miniai.model.InferenceConfig
import com.mubashir.miniai.model.InferenceResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AIInferenceService : Service() {

    @Inject
    lateinit var modelLoader: GGUFModelLoader

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    inner class LocalBinder : Binder() {
        fun getService(): AIInferenceService = this@AIInferenceService
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("AIInferenceService created")
    }

    override fun onBind(intent: Intent?): IBinder {
        Timber.d("AIInferenceService bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("AIInferenceService unbound")
        return super.onUnbind(intent)
    }

    suspend fun runInference(prompt: String, config: InferenceConfig): InferenceResult {
        return modelLoader.runInference(prompt, config)
    }

    override fun onDestroy() {
        super.onDestroy()
        modelLoader.cleanup()
        serviceScope.cancel()
        Timber.d("AIInferenceService destroyed")
    }
}
