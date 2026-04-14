package com.speakmate.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakmate.app.data.model.ChatMessage
import com.speakmate.app.data.repository.SpeakMateRepository
import com.speakmate.app.utils.OpenAIHelper
import kotlinx.coroutines.launch

/**
 * Drives the AI Conversation screen.
 * Requires an OpenAI API key set in Settings.
 */
class AIConversationViewModel(
    @Suppress("unused") private val repository: SpeakMateRepository
) : ViewModel() {

    private val _messages = MutableLiveData<MutableList<ChatMessage>>(mutableListOf())
    val messages: LiveData<MutableList<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isListening = MutableLiveData(false)
    val isListening: LiveData<Boolean> = _isListening

    // Holds the latest AI reply for TTS
    private val _latestReply = MutableLiveData<String?>()
    val latestReply: LiveData<String?> = _latestReply

    private var openAIHelper: OpenAIHelper? = null

    fun initialise(apiKey: String) {
        openAIHelper = OpenAIHelper(apiKey)
    }

    fun onListeningStateChanged(listening: Boolean) {
        _isListening.value = listening
    }

    fun sendMessage(userText: String) {
        val helper = openAIHelper
        if (helper == null || !helper.isConfigured()) {
            _error.value = "Please add your OpenAI API key in Settings to use AI Chat."
            return
        }

        // Add user message to chat
        val current = _messages.value ?: mutableListOf()
        current.add(ChatMessage(userText, isUser = true))
        _messages.value = current

        // Build history for API call
        val history = current.dropLast(1).map { msg ->
            Pair(if (msg.isUser) "user" else "assistant", msg.content)
        }

        _isLoading.value = true
        _error.value     = null

        viewModelScope.launch {
            try {
                val reply = helper.chat(userText, history)
                val updated = _messages.value ?: mutableListOf()
                updated.add(ChatMessage(reply, isUser = false))
                _messages.value  = updated
                _latestReply.value = reply
            } catch (e: Exception) {
                _error.value = "AI error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearLatestReply() { _latestReply.value = null }

    fun clearChat() {
        _messages.value = mutableListOf()
        _latestReply.value = null
        _error.value = null
    }
}
