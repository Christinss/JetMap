package com.example.jetmap.data.network.geoapify

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeoapifyDatasource(
    val raw: GeoapifyRaw? = null
)
