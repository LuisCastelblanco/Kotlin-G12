package com.example.explorandes.models

data class Recommendation(
    val id: String,
    val title: String,
    val description: String,
    val imageResId: Int,
    val type: RecommendationType,
    val url: String? = null
)

enum class RecommendationType {
    PODCAST,
    DOCUMENTARY,
    THEATER,
    EVENT
}