package com.example.geosphere.utils

object Constants {
    // Shared Preferences
    const val PREFS_NAME = "GeoSpherePrefs"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_NOTIFICATIONS = "notifications"
    const val KEY_SOUND_EFFECTS = "sound_effects"
    const val KEY_ANIMATIONS = "animations"

    // Quiz Settings
    const val DEFAULT_QUESTIONS_PER_QUIZ = 20
    const val TIME_PER_QUESTION = 30 // seconds
    const val POINTS_PER_CORRECT_ANSWER = 1

    // Achievement thresholds
    const val BRONZE_THRESHOLD = 25
    const val SILVER_THRESHOLD = 50
    const val GOLD_THRESHOLD = 75
    const val PLATINUM_THRESHOLD = 90
    const val DIAMOND_THRESHOLD = 100

    // Firebase paths
    const val USERS_PATH = "users"
    const val QUESTIONS_PATH = "questions"
    const val CATEGORIES_PATH = "categories"
    const val LEADERBOARD_PATH = "leaderboard"
    const val ACHIEVEMENTS_PATH = "achievements"
    const val USER_ANSWERS_PATH = "user_answers"

    // Animation durations
    const val SPLASH_DURATION = 2000L
    const val FADE_DURATION = 500L
    const val SLIDE_DURATION = 300L

    // Category icons
    val categoryIcons = mapOf(
        "asia" to "🌏",
        "europe" to "🌍",
        "americas" to "🌎",
        "africa" to "🌍",
        "flags" to "🏁",
        "capitals" to "🏛️",
        "landmarks" to "🗽",
        "world" to "🌐"
    )
}