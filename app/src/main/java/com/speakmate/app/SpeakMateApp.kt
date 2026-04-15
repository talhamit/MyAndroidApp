package com.speakmate.app

import android.app.Application
import android.util.Log

/**
 * Application class.
 *
 * FIX #6: Registers a global uncaught exception handler that logs crashes
 * to Logcat instead of silently killing the app. This makes debugging much
 * easier — look for tag "SpeakMate_CRASH" in logcat.
 */
class SpeakMateApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SpeakMate_CRASH", "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
