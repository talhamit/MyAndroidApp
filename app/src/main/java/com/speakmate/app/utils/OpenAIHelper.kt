package com.speakmate.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Lightweight OpenAI Chat Completions client.
 *
 * To enable: set your API key in Settings screen or pass it directly.
 * The feature degrades gracefully when no key is provided.
 *
 * NOTE: In a production app you would NEVER hard-code an API key.
 * Store it in EncryptedSharedPreferences or a secure backend proxy.
 */
class OpenAIHelper(private val apiKey: String) {

    companion object {
        private const val BASE_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL    = "gpt-3.5-turbo"

        private val SYSTEM_PROMPT = """
            You are a friendly English conversation tutor. 
            Keep responses short (2-3 sentences max).
            Gently correct grammar mistakes in the user's message.
            Use simple, clear English appropriate for language learners.
        """.trimIndent()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Send a chat message to OpenAI and return the assistant reply.
     * Throws an exception on network/API error.
     *
     * @param userMessage The text spoken/typed by the user.
     * @param history Previous (role, content) pairs for context.
     */
    suspend fun chat(
        userMessage: String,
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {

        val messages = JSONArray()

        // System prompt
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT)
        })

        // Conversation history (last 10 turns to stay within token budget)
        history.takeLast(10).forEach { (role, content) ->
            messages.put(JSONObject().apply {
                put("role", role)
                put("content", content)
            })
        }

        // Current user message
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", userMessage)
        })

        val body = JSONObject().apply {
            put("model", MODEL)
            put("messages", messages)
            put("max_tokens", 150)
            put("temperature", 0.7)
        }.toString()

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            val errorJson = JSONObject(responseBody)
            val errorMsg  = errorJson.optJSONObject("error")?.optString("message") ?: "API error"
            throw Exception(errorMsg)
        }

        val json = JSONObject(responseBody)
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()
    }

    /** Returns true if an API key has been provided. */
    fun isConfigured(): Boolean = apiKey.isNotBlank() && apiKey != "YOUR_API_KEY"
}
