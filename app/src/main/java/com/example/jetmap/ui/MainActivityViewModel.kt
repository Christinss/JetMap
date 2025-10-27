package com.example.jetmap.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetmap.data.LocationManager
import com.example.jetmap.data.network.ParkingSpot
import com.example.jetmap.data.network.ParkingSpotsResponse
import com.example.jetmap.data.repository.ParkingSpotsRepository
import com.example.jetmap.utils.AppConstants
import com.example.jetmap.utils.NetworkResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    private val parkingSpotsRepository: ParkingSpotsRepository,
    private val locationManager: LocationManager,
): ViewModel() {
    private val _zoomTrigger = MutableSharedFlow<LatLng>()
    val zoomTrigger: SharedFlow<LatLng> = _zoomTrigger
    private val _parkingSpotsState: MutableStateFlow<List<ParkingSpot>> = MutableStateFlow(listOf())
    val parkingSpotsState = _parkingSpotsState.asStateFlow()
    private val _parkingSpotState: MutableStateFlow<ParkingSpot?> = MutableStateFlow(null)
    val parkingSpotState = _parkingSpotState.asStateFlow()
    private val _errorMessages = MutableSharedFlow<ErrorMessage>()
    val errorMessages = _errorMessages.asSharedFlow()
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()
    private var isFetching = false

    fun requestInitialLocationZoom() {
        viewModelScope.launch {
            try {
                val location = locationManager.getLastAccurateLocation()
                Timber.d("Retrieved location: $location")
                location?.let { loc ->
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    Timber.d("Emitting zoomTrigger for: $latLng")
                    _zoomTrigger.emit(latLng)
                } ?: run {
                    Timber.w("No location available for initial zoom")
                    _errorMessages.emit(ErrorMessage.LocationFailed)
                }
            } catch (e: Exception) {
                Timber.e("Failed to get location for initial zoom: ${e.message}")
                _errorMessages.emit(ErrorMessage.LocationFailed)
            }
        }
    }

    fun getMapBounds(
        topRightLat: Double,
        topRightLng: Double,
        bottomLeftLat: Double,
        bottomLeftLng: Double
    ) {
        fetchParkingSpotsWithinBoundingBox(
            listOf(
                topRightLat,
                topRightLng,
                bottomLeftLat,
                bottomLeftLng
            ).joinToString(AppConstants.ParkingApi.COORDINATE_SEPARATOR)
        )
    }

    fun selectParkingSpot(parkingSpot: ParkingSpot) {
        _parkingSpotState.value = parkingSpot
    }

    fun clearSelectedParkingSpot() {
        _parkingSpotState.value = null
    }

    private fun fetchParkingSpotsWithinBoundingBox(
        boundingBox: String
    ) {
        if (isFetching) return

        viewModelScope.launch {
            isFetching = true
            _loadingState.value = true

            try {
                when (val result = parkingSpotsRepository.getParkingSpots(boundingBox)) {
                    is NetworkResult.Success -> {
                        result.data.let { response: ParkingSpotsResponse ->
                            _parkingSpotsState.value = response.parkingSpots
                        }
                    }

                    is NetworkResult.Error -> {
                        Timber.d("Error - ${result.code}: ${result.message}")
                        result.message?.let { message ->
                            _errorMessages.emit(ErrorMessage.NetworkErrorWithMessage(code = result.code, message = message))
                        } ?: _errorMessages.emit(ErrorMessage.NetworkError)
                    }

                    is NetworkResult.Exception -> {
                        Timber.d("Exception error - ${result.e.message}")
                        result.e.message?.let { message ->
                            _errorMessages.emit(ErrorMessage.NetworkExceptionWithMessage(message = message))
                        } ?: _errorMessages.emit(ErrorMessage.NetworkException)
                    }
                }
            } finally {
                isFetching = false
                _loadingState.value = false
            }
        }
    }
}
