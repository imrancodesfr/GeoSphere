package com.example.geosphere.models

data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val totalPoints: Int = 0,
    val quizzesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val rank: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)