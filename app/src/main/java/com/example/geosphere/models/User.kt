package com.example.geosphere.models

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val totalPoints: Int = 0,
    val quizzesPlayed: Int = 0,
    val correctAnswers: Int = 0,
    val achievements: MutableList<String> = mutableListOf(),
    val categoryScores: MutableMap<String, Int> = mutableMapOf(),
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis()
)