package com.speakmate.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakmate.app.data.model.ComparisonResult
import com.speakmate.app.data.model.PracticeSession
import com.speakmate.app.data.model.PracticeSentence
import com.speakmate.app.data.repository.ContentRepository
import com.speakmate.app.data.repository.SpeakMateRepository
import com.speakmate.app.utils.TextComparisonUtil
import kotlinx.coroutines.launch

/**
 * Drives the Speaking Practice screen.
 */
class PracticeViewModel(private val repository: SpeakMateRepository) : ViewModel() {

    // Current list of sentences for the selected category
    private val _sentences = MutableLiveData<List<PracticeSentence>>()
    val sentences: LiveData<List<PracticeSentence>> = _sentences

    // Index of the currently displayed sentence
    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    // The text recognised from microphone
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    // Result of comparing spoken vs expected
    private val _comparisonResult = MutableLiveData<ComparisonResult?>()
    val comparisonResult: LiveData<ComparisonResult?> = _comparisonResult

    // Whether the mic is active
    private val _isListening = MutableLiveData(false)
    val isListening: LiveData<Boolean> = _isListening

    // Session start time for duration tracking
    private var sessionStart = 0L

    fun loadSentences(category: String = "all") {
        val list = if (category == "all") ContentRepository.practiceSentences
        else ContentRepository.practiceSentences.filter { it.category == category }
        _sentences.value = list.shuffled()
        _currentIndex.value = 0
        _comparisonResult.value = null
        _recognizedText.value = ""
    }

    fun currentSentence(): PracticeSentence? {
        val list  = _sentences.value ?: return null
        val index = _currentIndex.value ?: 0
        return list.getOrNull(index)
    }

    fun onListeningStateChanged(listening: Boolean) {
        _isListening.value = listening
        if (listening) sessionStart = System.currentTimeMillis()
    }

    fun onSpeechResult(spokenText: String) {
        _recognizedText.value = spokenText
        val expected = currentSentence()?.text ?: return
        val result   = TextComparisonUtil.compare(expected, spokenText)
        _comparisonResult.value = result
        saveSession(expected, spokenText, result.accuracyScore)
    }

    fun onPartialResult(partial: String) {
        _recognizedText.value = partial
    }

    fun nextSentence() {
        val max = (_sentences.value?.size ?: 1) - 1
        val next = ((_currentIndex.value ?: 0) + 1).coerceAtMost(max)
        _currentIndex.value = next
        _comparisonResult.value = null
        _recognizedText.value = ""
    }

    fun previousSentence() {
        val prev = ((_currentIndex.value ?: 0) - 1).coerceAtLeast(0)
        _currentIndex.value = prev
        _comparisonResult.value = null
        _recognizedText.value = ""
    }

    private fun saveSession(sentence: String, spoken: String, score: Float) {
        val duration = ((System.currentTimeMillis() - sessionStart) / 1000).toInt()
        viewModelScope.launch {
            repository.saveSession(
                PracticeSession(
                    mode           = "PRACTICE",
                    sentence       = sentence,
                    spokenText     = spoken,
                    accuracyScore  = score,
                    durationSeconds = duration
                )
            )
        }
    }
}
