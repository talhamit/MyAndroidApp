package com.speakmate.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Wrapper around Android's SpeechRecognizer API.
 *
 * Usage:
 *   val sr = SpeechRecognizerHelper(context)
 *   sr.startListening { text -> /* handle result */ }
 *   sr.stopListening()
 *   sr.destroy()  // call in onDestroy
 */
class SpeechRecognizerHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onPartialResult: ((String) -> Unit)? = null,
    private val onError: ((String) -> Unit)? = null,
    private val onStateChange: ((Boolean) -> Unit)? = null  // true = listening
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    companion object {
        private const val TAG = "SpeechRecognizerHelper"

        /** Returns true if this device supports speech recognition. */
        fun isAvailable(context: Context): Boolean =
            SpeechRecognizer.isRecognitionAvailable(context)
    }

    init {
        initialize()
    }

    private fun initialize() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError?.invoke("Speech recognition is not available on this device.")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onStateChange?.invoke(true)
                Log.d(TAG, "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Volume level changes – can drive a waveform animation if needed
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                onStateChange?.invoke(false)
                Log.d(TAG, "Speech ended")
            }

            override fun onError(error: Int) {
                isListening = false
                onStateChange?.invoke(false)
                val msg = errorMessage(error)
                Log.e(TAG, "Error: $msg")
                onError?.invoke(msg)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onStateChange?.invoke(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                Log.d(TAG, "Final result: $text")
                if (text.isNotBlank()) onResult(text)
                else onError?.invoke("No speech detected. Please try again.")
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""
                if (partial.isNotBlank()) onPartialResult?.invoke(partial)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    /** Start listening. Language defaults to English (US). */
    fun startListening(languageCode: String = "en-US") {
        if (isListening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "startListening error: ${e.message}")
            onError?.invoke("Could not start listening: ${e.message}")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        onStateChange?.invoke(false)
    }

    fun cancel() {
        speechRecognizer?.cancel()
        isListening = false
        onStateChange?.invoke(false)
    }

    /** Must be called in Activity/Fragment onDestroy to avoid memory leaks. */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun isListening() = isListening

    // Re-create the recognizer (useful after errors)
    fun reset() {
        destroy()
        initialize()
    }

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO             -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT            -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission not granted"
        SpeechRecognizer.ERROR_NETWORK           -> "Network error – check your connection"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT   -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH          -> "No speech match found"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY   -> "Recognizer is busy"
        SpeechRecognizer.ERROR_SERVER            -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT    -> "No speech input detected"
        else                                     -> "Unknown error ($error)"
    }
}
