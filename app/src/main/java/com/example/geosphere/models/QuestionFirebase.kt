package com.example.geosphere.models

data class QuestionFirebase(
    val questionText: String = "",
    val options: List<String> = listOf(),
    val correctOptionIndex: Int = 0,
    val explanation: String = "",
    val difficulty: String = "medium",
    val points: Int = 1
)