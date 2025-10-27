package com.example.jetmap.data.network.geoapify

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoapifyRaw(
    @param:Json(name = "opening_hours")
    val openingHours: String? = null,
    val charge: String? = null
)
