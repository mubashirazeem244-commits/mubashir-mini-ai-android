package com.mubashir.miniai.data.repository

import com.mubashir.miniai.data.dao.ChatDao
import com.mubashir.miniai.data.entity.ChatEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ChatRepository(private val chatDao: ChatDao) {

    fun getAllChats(): Flow<List<ChatEntity>> {
        return chatDao.getAllChats()
    }

    fun getChatsBySession(sessionId: String): Flow<List<ChatEntity>> {
        return chatDao.getChatsBySession(sessionId)
    }

    suspend fun addChat(chat: ChatEntity): Long {
        return try {
            chatDao.insertChat(chat)
        } catch (e: Exception) {
            Timber.e(e, "Error adding chat")
            -1
        }
    }

    suspend fun deleteChat(chat: ChatEntity) {
        try {
            chatDao.deleteChat(chat)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting chat")
        }
    }

    suspend fun deleteSessionChats(sessionId: String) {
        try {
            chatDao.deleteSessionChats(sessionId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting session chats")
        }
    }

    suspend fun deleteAllChats() {
        try {
            chatDao.deleteAllChats()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting all chats")
        }
    }

    fun getChatCount(): Flow<Int> {
        return chatDao.getChatCount()
    }

    fun getTotalTokensGenerated(): Flow<Long> {
        return chatDao.getTotalTokensGenerated().let { flow ->
            kotlinx.coroutines.flow.map(flow) { it ?: 0L }
        }
    }
}

// Extension function for Flow mapping
private fun <T, R> kotlinx.coroutines.flow.Flow<T>.map(mapper: suspend (T) -> R): kotlinx.coroutines.flow.Flow<R> {
    return kotlinx.coroutines.flow.map(this, mapper)
}
