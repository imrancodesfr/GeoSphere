package com.example.geosphere.models

import com.example.geosphere.models.LeaderboardEntry

data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val totalPoints: Int = 0,
    val quizzesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val rank: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),

    // Daily leaderboard — resets each day
    val dailyPoints: Int = 0,
    val dailyKey: String = "",    // "2026-02-26"

    // Weekly leaderboard — resets each Monday
    val weeklyPoints: Int = 0,
    val weeklyKey: String = ""    // "2026-W09"
)