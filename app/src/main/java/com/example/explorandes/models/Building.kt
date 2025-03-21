package com.example.explorandes.models

data class Building(
    val id: String,
    val name: String,
    val location: String,
    val imageResId: Int,
    val description: String = "",
    val coordinates: Pair<Double, Double>? = null
)