package com.example.jetmap.data.network

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ParkingSpot(
    val name: String,
    @param:Json(name = "location")
    val parkingSpotLocation: ParkingSpotLocation,
    val address: String,
    val city: String,
    val country: String,
    val openingHours: String,
): ClusterItem, Parcelable {
    override fun getPosition(): LatLng = LatLng(
        parkingSpotLocation.latitude,
        parkingSpotLocation.longitude
    )
    override fun getTitle(): String? = null
    override fun getSnippet(): String? = null
    override fun getZIndex(): Float? = null
}
