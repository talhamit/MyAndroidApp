package com.speakmate.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakmate.app.data.model.PracticeSession
import com.speakmate.app.data.repository.ContentRepository
import com.speakmate.app.data.repository.SpeakMateRepository
import kotlinx.coroutines.launch

/**
 * Drives the Sentence Builder Game screen.
 * The user drags/taps shuffled words into the correct order.
 */
class GameViewModel(private val repository: SpeakMateRepository) : ViewModel() {

    // The correct sentence for the current round
    private val _correctSentence = MutableLiveData<String>()
    val correctSentence: LiveData<String> = _correctSentence

    // Shuffled word tiles shown to the user
    private val _shuffledWords = MutableLiveData<List<String>>()
    val shuffledWords: LiveData<List<String>> = _shuffledWords

    // Words the user has selected so far (their built sentence)
    private val _selectedWords = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedWords: LiveData<MutableList<String>> = _selectedWords

    // null = not checked yet; true = correct; false = wrong
    private val _isCorrect = MutableLiveData<Boolean?>()
    val isCorrect: LiveData<Boolean?> = _isCorrect

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _round = MutableLiveData(0)
    val round: LiveData<Int> = _round

    private val sentences = ContentRepository.gameSentences.shuffled().toMutableList()
    private var sentenceIndex = 0

    init { loadNextSentence() }

    fun loadNextSentence() {
        if (sentenceIndex >= sentences.size) sentenceIndex = 0
        val sentence = sentences[sentenceIndex++]
        _correctSentence.value = sentence
        _shuffledWords.value   = sentence.split(" ").shuffled()
        _selectedWords.value   = mutableListOf()
        _isCorrect.value       = null
        _round.value           = (_round.value ?: 0) + 1
    }

    /** User taps a word tile from the shuffled bank → move it to the answer area. */
    fun selectWord(word: String) {
        if (_isCorrect.value != null) return   // round already submitted
        val current = _selectedWords.value ?: mutableListOf()
        current.add(word)
        _selectedWords.value = current

        val shuffled = _shuffledWords.value?.toMutableList() ?: mutableListOf()
        shuffled.remove(word)
        _shuffledWords.value = shuffled
    }

    /** User taps a word in the answer area → send it back to the bank. */
    fun deselectWord(word: String) {
        if (_isCorrect.value != null) return
        val current = _selectedWords.value ?: return
        current.remove(word)
        _selectedWords.value = current

        val shuffled = _shuffledWords.value?.toMutableList() ?: mutableListOf()
        shuffled.add(word)
        _shuffledWords.value = shuffled
    }

    /** Check if the user's answer matches the correct sentence. */
    fun checkAnswer() {
        val built   = _selectedWords.value?.joinToString(" ") ?: ""
        val correct = _correctSentence.value ?: ""
        val isRight = built.trim().equals(correct.trim(), ignoreCase = true)
        _isCorrect.value = isRight
        if (isRight) _score.value = (_score.value ?: 0) + 10
        saveSession(correct, built, if (isRight) 100f else 0f)
    }

    private fun saveSession(sentence: String, attempt: String, score: Float) {
        viewModelScope.launch {
            repository.saveSession(
                PracticeSession(
                    mode          = "GAME",
                    sentence      = sentence,
                    spokenText    = attempt,
                    accuracyScore = score
                )
            )
        }
    }
}
