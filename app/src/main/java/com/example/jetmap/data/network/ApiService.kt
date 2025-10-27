package com.example.jetmap.data.network

import com.example.jetmap.data.network.geoapify.GeoapifyPlacesResponse
import com.example.jetmap.utils.ApiConstants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET(ApiConstants.PLACES_URL)
    suspend fun getParkingSpots(
        @Query("categories") categories: String,
        @Query("filter") filter: String,
        @Query("limit") limit: Int,
        @Query("apiKey") apiKey: String
    ): Response<GeoapifyPlacesResponse>
}