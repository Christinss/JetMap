package com.example.jetmap.data.network.geoapify

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoapifyProperties(
    val name: String? = null,
    val categories: List<String> = emptyList(),
    @param:Json(name = "address_line1")
    val addressLine1: String? = null,
    @param:Json(name = "address_line2")
    val addressLine2: String? = null,
    val city: String? = null,
    val country: String? = null,
    val formatted: String? = null,
    @param:Json(name = "opening_hours")
    val openingHours: String? = null,
    val datasource: GeoapifyDatasource? = null
)
