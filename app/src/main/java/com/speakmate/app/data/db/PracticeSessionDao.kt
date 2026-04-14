package com.speakmate.app.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.speakmate.app.data.model.PracticeSession

/**
 * Data Access Object for PracticeSession.
 * All DB operations run on background threads via coroutines.
 */
@Dao
interface PracticeSessionDao {

    /** Insert a new session; returns generated row ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSession): Long

    /** Load ALL sessions ordered by newest first – observed as LiveData. */
    @Query("SELECT * FROM practice_sessions ORDER BY date DESC")
    fun getAllSessions(): LiveData<List<PracticeSession>>

    /** Sessions from the last N days for the progress chart. */
    @Query("SELECT * FROM practice_sessions WHERE date >= :sinceTimestamp ORDER BY date DESC")
    suspend fun getSessionsSince(sinceTimestamp: Long): List<PracticeSession>

    /** Average accuracy across all sessions. */
    @Query("SELECT AVG(accuracyScore) FROM practice_sessions")
    suspend fun getAverageAccuracy(): Float?

    /** Sessions grouped by calendar day (epoch-day) for streak calculation. */
    @Query("SELECT date FROM practice_sessions ORDER BY date DESC")
    suspend fun getAllDates(): List<Long>

    /** Total number of sessions. */
    @Query("SELECT COUNT(*) FROM practice_sessions")
    suspend fun getTotalSessions(): Int

    /** Delete all sessions (reset progress). */
    @Query("DELETE FROM practice_sessions")
    suspend fun deleteAll()
}
