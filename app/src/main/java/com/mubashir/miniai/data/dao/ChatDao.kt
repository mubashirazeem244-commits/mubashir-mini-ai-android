package com.mubashir.miniai.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mubashir.miniai.data.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insertChat(chat: ChatEntity): Long

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getChatsBySession(sessionId: String): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Long): ChatEntity?

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE session_id = :sessionId")
    suspend fun deleteSessionChats(sessionId: String)

    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()

    @Query("SELECT COUNT(*) FROM chats")
    fun getChatCount(): Flow<Int>

    @Query("SELECT SUM(tokens_generated) FROM chats WHERE role = 'assistant'")
    fun getTotalTokensGenerated(): Flow<Long?>
}
