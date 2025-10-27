package com.example.jetmap.data.repository

import com.example.jetmap.BuildConfig
import com.example.jetmap.data.network.ApiService
import com.example.jetmap.data.network.ParkingSpot
import com.example.jetmap.data.network.ParkingSpotLocation
import com.example.jetmap.data.network.ParkingSpotsResponse
import com.example.jetmap.data.network.geoapify.GeoapifyProperties
import com.example.jetmap.utils.AppConstants
import com.example.jetmap.utils.NetworkResult
import com.example.jetmap.utils.TimeUtils
import timber.log.Timber
import javax.inject.Inject

class ParkingSpotsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getParkingSpots(boundingBox: String): NetworkResult<ParkingSpotsResponse> {
        return try {
            val coordinates = boundingBox.split(AppConstants.ParkingApi.COORDINATE_SEPARATOR)
            if (coordinates.size == AppConstants.ParkingApi.EXPECTED_COORDINATES_COUNT) {
                val topRightLat = coordinates[0].toDouble()
                val topRightLng = coordinates[1].toDouble()
                val bottomLeftLat = coordinates[2].toDouble()
                val bottomLeftLng = coordinates[3].toDouble()

                val filter = "${AppConstants.ParkingApi.FILTER_PREFIX}$bottomLeftLng,$bottomLeftLat,$topRightLng,$topRightLat"

                val response = apiService.getParkingSpots(
                    categories = AppConstants.ParkingApi.PARKING_CATEGORIES,
                    filter = filter,
                    limit = AppConstants.ParkingApi.PARKING_LIMIT,
                    apiKey = BuildConfig.GEOAPIFY_API_KEY
                )

                if (response.isSuccessful) {
                        val geoapifyResponse = response.body()
                        Timber.d("Geoapify response: ${geoapifyResponse?.features?.size} total features")

                    // Show parking spots
                    val parkingSpots = geoapifyResponse?.features
                        ?.filter { feature ->
                            val properties = feature.properties

                            // Ensure it's a parking facility
                            val isParking = properties.categories.any { category ->
                                category.contains(AppConstants.Validation.PARKING_FILTER)
                            }

                            // Ensure it has a valid name
                            val hasValidName = !properties.name.isNullOrEmpty() &&
                                    properties.name != AppConstants.Validation.UNKNOWN_NAME &&
                                    properties.name.trim().isNotEmpty()

                            isParking && hasValidName
                        }
                        ?.map { feature ->
                            // Transform to ParkingSpot model
                            ParkingSpot(
                                name = feature.properties.name ?: AppConstants.Fallbacks.DEFAULT_PARKING_NAME,
                                parkingSpotLocation = ParkingSpotLocation(
                                    latitude = feature.geometry.coordinates.getOrNull(1) ?: 0.0,
                                    longitude = feature.geometry.coordinates.getOrNull(0) ?: 0.0
                                ),
                                address = feature.properties.formatted
                                    ?: feature.properties.addressLine1 ?: feature.properties.addressLine2 ?: AppConstants.Fallbacks.DEFAULT_ADDRESS,
                                city = feature.properties.city ?: AppConstants.Fallbacks.DEFAULT_CITY,
                                country = feature.properties.country ?: AppConstants.Fallbacks.DEFAULT_COUNTRY,
                                openingHours = getOpeningHours(feature.properties) ?: AppConstants.Fallbacks.DEFAULT_HOURS,
                            )
                        } ?: emptyList()

                    Timber.d("Found ${parkingSpots.size} parking spots")

                    NetworkResult.Success(
                        ParkingSpotsResponse(
                            count = parkingSpots.size,
                            parkingSpots = parkingSpots
                        )
                    )
                } else {
                    NetworkResult.Error(response.code(), response.message())
                }
            } else {
                NetworkResult.Error(AppConstants.Errors.INVALID_BOUNDING_BOX_CODE, AppConstants.Errors.INVALID_BOUNDING_BOX_ERROR)
            }
        } catch (e: Exception) {
            NetworkResult.Exception(e)
        }
    }
    
    private fun getOpeningHours(properties: GeoapifyProperties): String? {
        // Try direct opening_hours first
        properties.openingHours?.let { return it }
        
        // Try nested opening_hours
        properties.datasource?.raw?.openingHours?.let { return it }
        
        // Try to extract opening hours from charge field
        properties.datasource?.raw?.charge?.let { charge ->
            val openingHoursFromCharge = TimeUtils.extractOpeningHoursFromCharge(charge)
            if (openingHoursFromCharge.isNotEmpty()) {
                return openingHoursFromCharge
            }
        }
        
        return null
    }
}