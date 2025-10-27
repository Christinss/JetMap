package com.example.jetmap.data.network.geoapify

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoapifyFeature(
    val type: String,
    val properties: GeoapifyProperties,
    val geometry: GeoapifyGeometry
)
