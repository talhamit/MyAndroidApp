package com.speakmate.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.speakmate.app.data.model.PracticeSession

/**
 * Single Room database instance for the app.
 * Uses a singleton pattern so only one instance is created.
 */
@Database(
    entities = [PracticeSession::class],
    version = 1,
    exportSchema = false
)
abstract class SpeakMateDatabase : RoomDatabase() {

    abstract fun practiceSessionDao(): PracticeSessionDao

    companion object {
        @Volatile
        private var INSTANCE: SpeakMateDatabase? = null

        fun getDatabase(context: Context): SpeakMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpeakMateDatabase::class.java,
                    "speakmate_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
