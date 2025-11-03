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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = false,
    val parkingSpot: ParkingSpot? = null,
    val parkingSpots: List<ParkingSpot> = emptyList(),
)

@HiltViewModel
class MainActivityViewModel
@Inject constructor(
    private val parkingSpotsRepository: ParkingSpotsRepository,
    private val locationManager: LocationManager,
): ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()
    private val _zoomTrigger = MutableSharedFlow<LatLng>()
    val zoomTrigger = _zoomTrigger.asSharedFlow()
    private val _errorMessages = MutableSharedFlow<ErrorMessage>()
    val errorMessages = _errorMessages.asSharedFlow()
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
        _uiState.update { it.copy(parkingSpot = parkingSpot) }
    }

    fun clearSelectedParkingSpot() {
        _uiState.update { it.copy(parkingSpot = null) }
    }

    private fun fetchParkingSpotsWithinBoundingBox(
        boundingBox: String
    ) {
        if (isFetching) return

        viewModelScope.launch {
            isFetching = true
            _uiState.update { it.copy(isLoading = true) }

            try {
                when (val result = parkingSpotsRepository.getParkingSpots(boundingBox)) {
                    is NetworkResult.Success -> {
                        result.data.let { response: ParkingSpotsResponse ->
                            _uiState.update { it.copy(parkingSpots = response.parkingSpots) }
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
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
