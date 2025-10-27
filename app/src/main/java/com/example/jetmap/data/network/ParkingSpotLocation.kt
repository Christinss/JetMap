package com.example.jetmap.data.network

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ParkingSpotLocation(
    @param:Json(name = "lng")
    val longitude: Double,
    @param:Json(name = "lat")
    val latitude: Double
): Parcelable