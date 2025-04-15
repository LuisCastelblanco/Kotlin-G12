package com.example.explorandes.models

import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null

)