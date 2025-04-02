package com.example.explorandes.models

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val profileImageUrl: String? = null,
    val role: String = "Student"
)