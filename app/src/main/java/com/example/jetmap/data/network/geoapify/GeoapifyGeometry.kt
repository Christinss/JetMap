package com.example.jetmap.data.network.geoapify

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoapifyGeometry(
    val type: String,
    val coordinates: List<Double> // [longitude, latitude]
)
