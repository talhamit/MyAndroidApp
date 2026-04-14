package com.speakmate.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Centralised preferences manager.
 * Stores user settings and the optional OpenAI API key.
 */
class PrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("speakmate_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY      = "openai_api_key"
        private const val KEY_SPEECH_RATE  = "speech_rate"
        private const val KEY_SHOW_TIPS    = "show_tips"
        private const val KEY_DAILY_GOAL   = "daily_goal_minutes"
        private const val KEY_ONBOARDED    = "onboarded"
    }

    var openAiApiKey: String
        get()      = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_API_KEY, value) }

    /** Speech rate: 0.5 (slow) → 1.5 (fast). Default 0.9. */
    var speechRate: Float
        get()      = prefs.getFloat(KEY_SPEECH_RATE, 0.9f)
        set(value) = prefs.edit { putFloat(KEY_SPEECH_RATE, value) }

    var showTips: Boolean
        get()      = prefs.getBoolean(KEY_SHOW_TIPS, true)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_TIPS, value) }

    /** Daily practice goal in minutes. Default 10. */
    var dailyGoalMinutes: Int
        get()      = prefs.getInt(KEY_DAILY_GOAL, 10)
        set(value) = prefs.edit { putInt(KEY_DAILY_GOAL, value) }

    var hasOnboarded: Boolean
        get()      = prefs.getBoolean(KEY_ONBOARDED, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDED, value) }
}
