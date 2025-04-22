package com.example.explorandes.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Building(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("category") val category: String = "Buildings",

    // Exclude places from serialization/deserialization to prevent circular reference
    @Expose(serialize = false, deserialize = false)
    @SerializedName("places") val places: List<Place>? = null
)