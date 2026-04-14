package com.speakmate.app.data.repository

import com.speakmate.app.data.model.ConversationTurn
import com.speakmate.app.data.model.PracticeSentence

/**
 * Static content provider.  All data is embedded in the app so it works
 * fully offline – no network call required.
 */
object ContentRepository {

    // ─────────────────────────────────────────────
    // PRACTICE SENTENCES
    // ─────────────────────────────────────────────

    val practiceSentences: List<PracticeSentence> = listOf(
        // Basic
        PracticeSentence(1, "Good morning, how are you today?", "basic", "Stress 'morning' and 'today'"),
        PracticeSentence(2, "My name is Alex and I am from New York.", "basic", "Speak clearly and slowly"),
        PracticeSentence(3, "Could you please repeat that for me?", "basic", "Use a polite rising intonation"),
        PracticeSentence(4, "I would like a cup of coffee, please.", "basic", "Stress 'coffee'"),
        PracticeSentence(5, "What time does the next bus arrive?", "basic", ""),
        PracticeSentence(6, "Thank you very much for your help.", "basic", ""),
        PracticeSentence(7, "I am looking for the nearest pharmacy.", "basic", ""),
        PracticeSentence(8, "How much does this item cost?", "basic", ""),

        // Intermediate
        PracticeSentence(9,  "I have been working on this project for three weeks.", "intermediate", ""),
        PracticeSentence(10, "Could you explain the difference between these two options?", "intermediate", ""),
        PracticeSentence(11, "I would appreciate it if you could send me the details by email.", "intermediate", ""),
        PracticeSentence(12, "Despite the challenges, we managed to complete the task on time.", "intermediate", ""),
        PracticeSentence(13, "She mentioned that she had already submitted her application.", "intermediate", ""),
        PracticeSentence(14, "The conference will be held in downtown Chicago next month.", "intermediate", ""),

        // Advanced
        PracticeSentence(15, "The implementation of new policies has significantly impacted productivity.", "advanced", ""),
        PracticeSentence(16, "Notwithstanding the unforeseen circumstances, we remained committed to our objectives.", "advanced", ""),
        PracticeSentence(17, "The research findings suggest a correlation between sleep quality and cognitive performance.", "advanced", ""),
        PracticeSentence(18, "She eloquently articulated the nuances of the proposed legislative amendment.", "advanced", "")
    )

    // ─────────────────────────────────────────────
    // DAILY CONVERSATION TURNS
    // ─────────────────────────────────────────────

    val conversationTurns: List<ConversationTurn> = listOf(
        // Restaurant
        ConversationTurn(1, "Welcome! Do you have a reservation?",
            "Yes, I have a reservation under the name Johnson for two.", "restaurant"),
        ConversationTurn(2, "Are you ready to order?",
            "Yes please. I would like the grilled salmon and a side salad.", "restaurant"),
        ConversationTurn(3, "Would you like anything to drink?",
            "I would love a sparkling water and a glass of orange juice.", "restaurant"),
        ConversationTurn(4, "How was everything?",
            "It was absolutely delicious! The salmon was cooked perfectly.", "restaurant"),
        ConversationTurn(5, "Can I get you the check?",
            "Yes please. And could we split the bill between two cards?", "restaurant"),

        // Job Interview
        ConversationTurn(6, "Tell me a little about yourself.",
            "I am a software developer with five years of experience building mobile applications.", "job_interview"),
        ConversationTurn(7, "What is your greatest strength?",
            "My greatest strength is my ability to solve complex problems under pressure.", "job_interview"),
        ConversationTurn(8, "Where do you see yourself in five years?",
            "I hope to be leading a team and contributing to impactful products.", "job_interview"),
        ConversationTurn(9, "Why do you want to work for our company?",
            "I admire your commitment to innovation and your collaborative culture.", "job_interview"),
        ConversationTurn(10, "Do you have any questions for us?",
            "Yes, could you describe what a typical day looks like in this role?", "job_interview"),

        // Daily Talk
        ConversationTurn(11, "How was your weekend?",
            "It was great! I went hiking with some friends on Saturday.", "daily_talk"),
        ConversationTurn(12, "Did you watch the game last night?",
            "I missed it but I heard it was really exciting. Who won?", "daily_talk"),
        ConversationTurn(13, "What are you planning to do this evening?",
            "I am thinking of cooking dinner at home and watching a movie.", "daily_talk"),
        ConversationTurn(14, "Have you tried that new café on Main Street?",
            "Not yet, but I have heard great things about their pastries.", "daily_talk"),
        ConversationTurn(15, "It looks like it might rain today.",
            "You are right. I should probably bring an umbrella just in case.", "daily_talk")
    )

    // ─────────────────────────────────────────────
    // SENTENCE BUILDER GAME SENTENCES
    // ─────────────────────────────────────────────

    val gameSentences: List<String> = listOf(
        "The cat sat on the mat",
        "I love learning English every day",
        "She is reading a very interesting book",
        "We went to the park yesterday afternoon",
        "They are going to travel to London next summer",
        "He always drinks coffee in the morning",
        "The children played football in the garden",
        "My friend works at a big technology company",
        "Please turn off the lights before you leave",
        "I cannot wait to see you again soon"
    )
}
