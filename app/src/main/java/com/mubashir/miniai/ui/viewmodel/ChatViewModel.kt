package com.mubashir.miniai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubashir.miniai.data.entity.ChatEntity
import com.mubashir.miniai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<ChatEntity>>(emptyList())
    val chats: StateFlow<List<ChatEntity>> = _chats.asStateFlow()

    private val _chatCount = MutableStateFlow(0)
    val chatCount: StateFlow<Int> = _chatCount.asStateFlow()

    private val _totalTokens = MutableStateFlow(0L)
    val totalTokens: StateFlow<Long> = _totalTokens.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            try {
                chatRepository.getAllChats().collect { chats ->
                    _chats.value = chats
                    Timber.d("Loaded ${chats.size} chats")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading chats")
                _uiState.value = ChatUiState.Error("Failed to load chats")
            }
        }

        viewModelScope.launch {
            try {
                chatRepository.getChatCount().collect { count ->
                    _chatCount.value = count
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading chat count")
            }
        }

        viewModelScope.launch {
            try {
                chatRepository.getTotalTokensGenerated().collect { tokens ->
                    _totalTokens.value = tokens
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading total tokens")
            }
        }
    }

    fun addChat(role: String, content: String, sessionId: String = "") {
        viewModelScope.launch {
            try {
                val chat = ChatEntity(
                    role = role,
                    content = content,
                    sessionId = sessionId
                )
                chatRepository.addChat(chat)
                Timber.d("Chat added: $role")
            } catch (e: Exception) {
                Timber.e(e, "Error adding chat")
                _uiState.value = ChatUiState.Error("Failed to add chat")
            }
        }
    }

    fun addAssistantResponse(content: String, tokensGenerated: Int = 0, inferenceTimeMs: Long = 0, sessionId: String = "") {
        viewModelScope.launch {
            try {
                val chat = ChatEntity(
                    role = "assistant",
                    content = content,
                    tokensGenerated = tokensGenerated,
                    inferenceTimeMs = inferenceTimeMs,
                    sessionId = sessionId
                )
                chatRepository.addChat(chat)
            } catch (e: Exception) {
                Timber.e(e, "Error adding assistant response")
            }
        }
    }

    fun deleteChat(chat: ChatEntity) {
        viewModelScope.launch {
            try {
                chatRepository.deleteChat(chat)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting chat")
                _uiState.value = ChatUiState.Error("Failed to delete chat")
            }
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            try {
                chatRepository.deleteAllChats()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing chats")
                _uiState.value = ChatUiState.Error("Failed to clear chats")
            }
        }
    }
}

sealed class ChatUiState {
    object Idle : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}
