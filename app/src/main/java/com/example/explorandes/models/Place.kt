package com.example.explorandes.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("floor") val floor: String? = null,
    @SerializedName("coordinates") val coordinates: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,

    // Use building_id directly rather than a full building object
    @SerializedName("building_id") val buildingId: Long? = null,

    // Exclude the full building from serialization/deserialization
    @Expose(serialize = false, deserialize = false)
    @SerializedName("building") val building: Building? = null
) {
    // Helper function with a different name to avoid conflicts
    fun getEffectiveBuildingId(): Long? {
        return buildingId ?: building?.id
    }
}