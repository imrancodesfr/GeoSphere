package com.example.geosphere.models

data class Category(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val description: String = "",
    val questionCount: Int = 0,
    val color: String = "#3498DB",
    val achievements: MutableList<Achievement> = mutableListOf()
)