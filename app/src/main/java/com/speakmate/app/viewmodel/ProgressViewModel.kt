package com.speakmate.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakmate.app.data.model.PracticeSession
import com.speakmate.app.data.model.StreakInfo
import com.speakmate.app.data.repository.SpeakMateRepository
import kotlinx.coroutines.launch

/**
 * Drives the Progress screen.
 */
class ProgressViewModel(private val repository: SpeakMateRepository) : ViewModel() {

    /** All sessions observed live from Room. */
    val allSessions: LiveData<List<PracticeSession>> = repository.allSessions

    private val _averageAccuracy = MutableLiveData<Float>()
    val averageAccuracy: LiveData<Float> = _averageAccuracy

    private val _streakInfo = MutableLiveData<StreakInfo>()
    val streakInfo: LiveData<StreakInfo> = _streakInfo

    private val _totalSessions = MutableLiveData<Int>()
    val totalSessions: LiveData<Int> = _totalSessions

    private val _recentSessions = MutableLiveData<List<PracticeSession>>()
    val recentSessions: LiveData<List<PracticeSession>> = _recentSessions

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            _averageAccuracy.value = repository.getAverageAccuracy()
            _streakInfo.value      = repository.getStreakInfo()
            _totalSessions.value   = repository.getTotalSessions()
            _recentSessions.value  = repository.getRecentSessions()
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            loadStats()
        }
    }
}
