package com.speakmate.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakmate.app.data.model.ConversationTurn
import com.speakmate.app.data.model.PracticeSession
import com.speakmate.app.data.repository.ContentRepository
import com.speakmate.app.data.repository.SpeakMateRepository
import com.speakmate.app.utils.TextComparisonUtil
import kotlinx.coroutines.launch

/**
 * Drives the Daily Conversation mode screen.
 */
class ConversationViewModel(private val repository: SpeakMateRepository) : ViewModel() {

    private val _turns = MutableLiveData<List<ConversationTurn>>()
    val turns: LiveData<List<ConversationTurn>> = _turns

    private val _currentTurnIndex = MutableLiveData(0)
    val currentTurnIndex: LiveData<Int> = _currentTurnIndex

    private val _userReply = MutableLiveData<String>()
    val userReply: LiveData<String> = _userReply

    private val _feedback = MutableLiveData<String>()
    val feedback: LiveData<String> = _feedback

    private val _score = MutableLiveData<Float>()
    val score: LiveData<Float> = _score

    private val _isListening = MutableLiveData(false)
    val isListening: LiveData<Boolean> = _isListening

    fun loadCategory(category: String) {
        val list = ContentRepository.conversationTurns.filter { it.category == category }
        _turns.value = list
        _currentTurnIndex.value = 0
        clearFeedback()
    }

    fun currentTurn(): ConversationTurn? {
        val list  = _turns.value ?: return null
        val index = _currentTurnIndex.value ?: 0
        return list.getOrNull(index)
    }

    fun onListeningStateChanged(listening: Boolean) {
        _isListening.value = listening
    }

    fun onUserReply(spoken: String) {
        _userReply.value = spoken
        val turn = currentTurn() ?: return
        val score = TextComparisonUtil.quickScore(turn.sampleAnswer, spoken)
        _score.value = score
        _feedback.value = buildFeedback(score, turn.sampleAnswer)
        saveSession(turn, spoken, score)
    }

    private fun buildFeedback(score: Float, sampleAnswer: String): String {
        val grade = TextComparisonUtil.gradeLabel(score)
        return "$grade\n\nSample answer:\n\"$sampleAnswer\""
    }

    fun nextTurn() {
        val max = (_turns.value?.size ?: 1) - 1
        _currentTurnIndex.value = ((_currentTurnIndex.value ?: 0) + 1).coerceAtMost(max)
        clearFeedback()
    }

    fun clearFeedback() {
        _userReply.value = ""
        _feedback.value = ""
        _score.value = 0f
    }

    fun isLastTurn(): Boolean {
        val list  = _turns.value ?: return true
        val index = _currentTurnIndex.value ?: 0
        return index >= list.size - 1
    }

    private fun saveSession(turn: ConversationTurn, spoken: String, score: Float) {
        viewModelScope.launch {
            repository.saveSession(
                PracticeSession(
                    mode          = "CONVERSATION",
                    sentence      = turn.question,
                    spokenText    = spoken,
                    accuracyScore = score
                )
            )
        }
    }
}
