package com.speakmate.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single practice session result stored in Room DB.
 */
@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),   // epoch ms
    val mode: String,                               // e.g. "PRACTICE", "CONVERSATION", "GAME"
    val sentence: String,
    val spokenText: String,
    val accuracyScore: Float,                       // 0.0 – 100.0
    val durationSeconds: Int = 0
)

/**
 * Represents daily streak info.
 */
data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastPracticeDate: Long
)

/**
 * A predefined sentence used in Speaking Practice.
 */
data class PracticeSentence(
    val id: Int,
    val text: String,
    val category: String,   // "basic", "intermediate", "advanced"
    val tip: String = ""
)

/**
 * A conversation turn (question + expected answer).
 */
data class ConversationTurn(
    val id: Int,
    val question: String,
    val sampleAnswer: String,
    val category: String    // "restaurant", "job_interview", "daily_talk"
)

/**
 * Result of comparing spoken text to expected text.
 */
data class ComparisonResult(
    val expectedWords: List<WordResult>,
    val accuracyScore: Float,
    val correctCount: Int,
    val totalCount: Int
)

/**
 * Single word comparison outcome.
 */
data class WordResult(
    val word: String,
    val status: WordStatus   // CORRECT, WRONG, MISSING
)

enum class WordStatus { CORRECT, WRONG, MISSING }

/**
 * Chat message used in AI Conversation mode.
 */
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
