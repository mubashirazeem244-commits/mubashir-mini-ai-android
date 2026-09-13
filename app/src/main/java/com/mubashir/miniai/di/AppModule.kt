package com.mubashir.miniai.di

import android.content.Context
import androidx.room.Room
import com.mubashir.miniai.data.db.AppDatabase
import com.mubashir.miniai.data.dao.ChatDao
import com.mubashir.miniai.data.dao.ModelDao
import com.mubashir.miniai.data.repository.ChatRepository
import com.mubashir.miniai.data.repository.ModelRepository
import com.mubashir.miniai.llm.GGUFModelLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mubashir_mini_ai.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }

    @Singleton
    @Provides
    fun provideModelDao(database: AppDatabase): ModelDao {
        return database.modelDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideChatRepository(chatDao: ChatDao): ChatRepository {
        return ChatRepository(chatDao)
    }

    @Singleton
    @Provides
    fun provideModelRepository(modelDao: ModelDao): ModelRepository {
        return ModelRepository(modelDao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object LLMModule {

    @Singleton
    @Provides
    fun provideGGUFModelLoader(@ApplicationContext context: Context): GGUFModelLoader {
        return GGUFModelLoader(context)
    }
}
