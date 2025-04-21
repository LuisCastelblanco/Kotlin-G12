package com.example.explorandes.models

enum class RecommendationType {
    PODCAST,
    DOCUMENTARY,
    THEATER,
    EVENT
}

data class Recommendation(
    val id: Long,
    val title: String,
    val description: String,
    val imageResId: Int,  // Resource ID for image
    val type: RecommendationType
)