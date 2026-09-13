package com.mubashir.miniai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mubashir.miniai.data.entity.ChatEntity
import com.mubashir.miniai.data.entity.ModelEntity
import com.mubashir.miniai.data.dao.ChatDao
import com.mubashir.miniai.data.dao.ModelDao

@Database(
    entities = [ChatEntity::class, ModelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun modelDao(): ModelDao
}
