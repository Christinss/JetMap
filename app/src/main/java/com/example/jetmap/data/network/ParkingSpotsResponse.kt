package com.example.jetmap.data.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParkingSpotsResponse(
    val count: Int,
    val parkingSpots: List<ParkingSpot>
)