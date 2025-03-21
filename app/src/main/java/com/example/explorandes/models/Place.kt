package com.example.explorandes.models

data class Place(
    val id: Int,
    val name: String,
    val code: String,
    val category: String,
    val distance: String,
    val coordinates: String,
    val imageResId: Int
)