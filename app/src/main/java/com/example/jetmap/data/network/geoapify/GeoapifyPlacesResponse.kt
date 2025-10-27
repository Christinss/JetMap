package com.example.jetmap.data.network.geoapify

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoapifyPlacesResponse(
    val type: String,
    val features: List<GeoapifyFeature>
)