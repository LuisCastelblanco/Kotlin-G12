package com.example.explorandes.models

data class AuthResponse(
    val token: String,
    val id: Long? = null,
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val message: String? = null,
    val user: User? = null
)