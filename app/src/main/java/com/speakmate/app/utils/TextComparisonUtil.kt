package com.speakmate.app.utils

import com.speakmate.app.data.model.ComparisonResult
import com.speakmate.app.data.model.WordResult
import com.speakmate.app.data.model.WordStatus

/**
 * Utility for comparing spoken text against expected text.
 * Produces a word-level diff with accuracy score.
 */
object TextComparisonUtil {

    /**
     * Compare [spoken] with [expected] at the word level.
     *
     * Algorithm:
     *  1. Normalise both strings (lowercase, remove punctuation).
     *  2. Walk through expected words; mark each as CORRECT, WRONG, or MISSING
     *     based on whether the spoken list contains the word (in order).
     *  3. Calculate accuracy as (correct / total-expected) * 100.
     */
    fun compare(expected: String, spoken: String): ComparisonResult {
        val expectedWords = normalize(expected)
        val spokenWords   = normalize(spoken).toMutableList()

        val results = mutableListOf<WordResult>()
        var correct = 0

        for (word in expectedWords) {
            val idx = spokenWords.indexOf(word)
            when {
                idx >= 0 -> {
                    results.add(WordResult(word, WordStatus.CORRECT))
                    spokenWords.removeAt(idx)   // consume the match
                    correct++
                }
                spokenWords.isNotEmpty() -> {
                    // Take the next spoken word and mark original as WRONG
                    val mispronounced = spokenWords.removeAt(0)
                    results.add(WordResult(mispronounced, WordStatus.WRONG))
                }
                else -> {
                    results.add(WordResult(word, WordStatus.MISSING))
                }
            }
        }

        val total = expectedWords.size
        val accuracy = if (total > 0) (correct.toFloat() / total * 100f) else 0f

        return ComparisonResult(
            expectedWords = results,
            accuracyScore = accuracy,
            correctCount  = correct,
            totalCount    = total
        )
    }

    /**
     * Quick accuracy score (0-100) without full word diff.
     * Uses Jaro-Winkler-like token overlap for a fair score
     * even when word order differs slightly.
     */
    fun quickScore(expected: String, spoken: String): Float {
        val exp = normalize(expected)
        val spk = normalize(spoken).toMutableList()
        if (exp.isEmpty()) return 100f
        var matches = 0
        for (w in exp) {
            if (spk.remove(w)) matches++
        }
        return matches.toFloat() / exp.size * 100f
    }

    /** Normalize: lowercase, remove punctuation, split on whitespace. */
    private fun normalize(text: String): List<String> =
        text.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

    /** Grade label for a given score. */
    fun gradeLabel(score: Float): String = when {
        score >= 95 -> "Excellent! 🎉"
        score >= 80 -> "Great job! 👍"
        score >= 60 -> "Good effort! Keep practicing."
        score >= 40 -> "Keep going! You're improving."
        else        -> "Let's try again! 💪"
    }

    /** Colour resource name suggestion based on score. */
    fun gradeColor(score: Float): String = when {
        score >= 80 -> "correct_green"
        score >= 50 -> "warning_amber"
        else        -> "error_red"
    }
}
