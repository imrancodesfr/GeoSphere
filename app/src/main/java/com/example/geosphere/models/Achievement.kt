package com.example.geosphere.models

data class Achievement(
    val id: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val requiredScore: Int = 100,
    val achievedBy: MutableList<String> = mutableListOf()
)