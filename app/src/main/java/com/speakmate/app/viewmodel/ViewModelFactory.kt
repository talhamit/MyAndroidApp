package com.speakmate.app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.speakmate.app.data.db.SpeakMateDatabase
import com.speakmate.app.data.repository.SpeakMateRepository

/**
 * Factory to provide the Repository dependency to ViewModels.
 */
class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    private val repository: SpeakMateRepository by lazy {
        val db = SpeakMateDatabase.getDatabase(application)
        SpeakMateRepository(db.practiceSessionDao())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PracticeViewModel::class.java) ->
                PracticeViewModel(repository) as T
            modelClass.isAssignableFrom(ConversationViewModel::class.java) ->
                ConversationViewModel(repository) as T
            modelClass.isAssignableFrom(GameViewModel::class.java) ->
                GameViewModel(repository) as T
            modelClass.isAssignableFrom(ProgressViewModel::class.java) ->
                ProgressViewModel(repository) as T
            modelClass.isAssignableFrom(AIConversationViewModel::class.java) ->
                AIConversationViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
