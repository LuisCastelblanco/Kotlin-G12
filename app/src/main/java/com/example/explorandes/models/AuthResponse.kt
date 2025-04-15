package com.example.explorandes.models

data class AuthResponse(
    val token: String,
    val user: User? = null,
    val message: String? = null
)