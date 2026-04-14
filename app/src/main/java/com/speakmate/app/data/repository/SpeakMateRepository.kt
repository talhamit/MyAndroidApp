package com.speakmate.app.data.repository

import androidx.lifecycle.LiveData
import com.speakmate.app.data.db.PracticeSessionDao
import com.speakmate.app.data.model.PracticeSession
import com.speakmate.app.data.model.StreakInfo
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Repository acts as the single source of truth for all data.
 * ViewModels interact with data exclusively through this class.
 */
class SpeakMateRepository(private val dao: PracticeSessionDao) {

    /** Observe all sessions as LiveData. */
    val allSessions: LiveData<List<PracticeSession>> = dao.getAllSessions()

    /** Save a completed practice session. */
    suspend fun saveSession(session: PracticeSession): Long {
        return dao.insertSession(session)
    }

    /** Get average accuracy score across all sessions. */
    suspend fun getAverageAccuracy(): Float {
        return dao.getAverageAccuracy() ?: 0f
    }

    /** Get sessions from the past 30 days. */
    suspend fun getRecentSessions(): List<PracticeSession> {
        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
        return dao.getSessionsSince(thirtyDaysAgo)
    }

    /** Get total session count. */
    suspend fun getTotalSessions(): Int = dao.getTotalSessions()

    /**
     * Calculate current streak and longest streak by checking
     * consecutive calendar days with at least one session.
     */
    suspend fun getStreakInfo(): StreakInfo {
        val dates = dao.getAllDates()
        if (dates.isEmpty()) return StreakInfo(0, 0, 0)

        // Convert timestamps to unique epoch-day values (days since Jan 1 1970)
        val days = dates.map { ts ->
            val cal = Calendar.getInstance().apply { timeInMillis = ts }
            val y = cal.get(Calendar.YEAR)
            val doy = cal.get(Calendar.DAY_OF_YEAR)
            y * 1000 + doy   // simple unique day key
        }.toSortedSet().toList().sortedDescending()

        var current = 1
        var longest = 1
        var temp = 1

        val todayKey = Calendar.getInstance().let { c ->
            c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
        }

        // Check if the most recent session was today or yesterday
        val mostRecent = days.first()
        if (mostRecent < todayKey - 1) {
            current = 0 // streak broken
        }

        for (i in 1 until days.size) {
            // A consecutive day has a key exactly 1 less (ignore year boundary edge case)
            val diff = days[i - 1] - days[i]
            if (diff == 1) {
                temp++
                if (temp > longest) longest = temp
                if (i < (current.coerceAtLeast(1))) current = temp
            } else {
                temp = 1
            }
        }
        if (current == 0) current = 0

        return StreakInfo(
            currentStreak = if (mostRecent >= todayKey - 1) current else 0,
            longestStreak = longest,
            lastPracticeDate = dates.first()
        )
    }

    /** Delete all sessions (for reset). */
    suspend fun resetProgress() = dao.deleteAll()
}
