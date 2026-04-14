package com.speakmate.app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID

/**
 * Clean wrapper around Android TextToSpeech.
 *
 * Usage:
 *   val tts = TextToSpeechHelper(context)
 *   tts.speak("Hello world")
 *   tts.destroy()   // call in onDestroy
 */
class TextToSpeechHelper(
    context: Context,
    private val onDone: (() -> Unit)? = null,
    private val onStart: (() -> Unit)? = null,
    private val onError: ((String) -> Unit)? = null
) {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingSpeech: String? = null

    companion object {
        private const val TAG = "TextToSpeechHelper"
    }

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isReady = if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                    onError?.invoke("English TTS language data is missing")
                    false
                } else {
                    // Set speech rate and pitch
                    tts?.setSpeechRate(0.9f)   // slightly slower for learners
                    tts?.setPitch(1.0f)
                    setupProgressListener()
                    true
                }
                // Speak anything queued before TTS was ready
                pendingSpeech?.let { speak(it); pendingSpeech = null }
            } else {
                Log.e(TAG, "TextToSpeech init failed with status: $status")
                onError?.invoke("TextToSpeech initialization failed")
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { onStart?.invoke() }
            override fun onDone(utteranceId: String?)  { onDone?.invoke() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { onError?.invoke("TTS playback error") }
        })
    }

    /**
     * Speak the given text aloud.
     * @param text The text to be spoken.
     * @param flush If true, stop any current speech and start immediately.
     */
    fun speak(text: String, flush: Boolean = true) {
        if (!isReady) {
            pendingSpeech = text   // queue for when TTS is ready
            return
        }
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, queueMode, null, utteranceId)
    }

    /** Stop speaking immediately. */
    fun stop() {
        tts?.stop()
    }

    /** Pause (only supported on API 26+). */
    fun pause() {
        if (android.os.Build.VERSION.SDK_INT >= 26) tts?.stop()
    }

    /** Returns true if TTS engine is currently synthesizing speech. */
    fun isSpeaking(): Boolean = tts?.isSpeaking ?: false

    /** Change speech rate (0.5 = slow, 1.0 = normal, 2.0 = fast). */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.1f, 3.0f))
    }

    /** Must be called in Activity/Fragment onDestroy to release resources. */
    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
