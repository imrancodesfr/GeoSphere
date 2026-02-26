package com.example.geosphere.models

data class Question(
    val id: String = "",
    val questionText: String = "",
    val options: List<String> = listOf(),
    val correctOptionIndex: Int = 0,
    val explanation: String = "",
    val difficulty: String = "medium",
    val points: Int = 1,
    val createdAt: Long = 0,
    val timesUsed: Int = 0,
    val timesCorrect: Int = 0
)


