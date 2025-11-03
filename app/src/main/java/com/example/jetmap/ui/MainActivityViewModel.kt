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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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
    private val _mapBounds = MutableSharedFlow<String>(extraBufferCapacity = 1)


    init {
        observeMapBounds()
    }

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
                Timber.e(e, "Failed to get location for initial zoom")
                _errorMessages.emit(ErrorMessage.LocationFailed)
            }
        }
    }

    fun onMapBoundsChanged(
        topRightLat: Double,
        topRightLng: Double,
        bottomLeftLat: Double,
        bottomLeftLng: Double
    ) {
        val boundingBox =listOf(
                topRightLat,
                topRightLng,
                bottomLeftLat,
                bottomLeftLng
            ).joinToString(AppConstants.ParkingApi.COORDINATE_SEPARATOR)
        _mapBounds.tryEmit(boundingBox)
    }

    fun selectParkingSpot(parkingSpot: ParkingSpot) {
        _uiState.update { it.copy(parkingSpot = parkingSpot) }
    }

    fun clearSelectedParkingSpot() {
        _uiState.update { it.copy(parkingSpot = null) }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeMapBounds() {
        viewModelScope.launch {
            _mapBounds
                .distinctUntilChanged()
                .flatMapLatest { boundingBox ->
                    flow {
                        _uiState.update { it.copy(isLoading = true) }
                        emit(parkingSpotsRepository.getParkingSpots(boundingBox))
                    }
                }
                .catch { e -> handleNetworkException(e) }
                .collect { result ->
                    _uiState.update { it.copy(isLoading = false) }

                    when (result) {
                        is NetworkResult.Success -> handleParkingSpotsSuccess(result.data)
                        is NetworkResult.Error -> handleNetworkError(result)
                        is NetworkResult.Exception -> handleNetworkException(result.e)
                    }
                }
        }
    }

    private fun handleParkingSpotsSuccess(response: ParkingSpotsResponse) {
        _uiState.update { it.copy(parkingSpots = response.parkingSpots) }
        Timber.d("Fetched ${response.parkingSpots.size} parking spots")
    }

    private suspend fun handleNetworkError(result: NetworkResult.Error<*>) {
        Timber.w("Network error - code: ${result.code}, message: ${result.message}")
        val message = result.message?.let {
            ErrorMessage.NetworkErrorInfo(code = result.code, message = it)
        } ?: ErrorMessage.NetworkError

        _errorMessages.emit(message)
    }

    private suspend fun handleNetworkException(e: Throwable) {
        Timber.e(e, "Network exception occurred")
        val message = e.message?.let {
            ErrorMessage.NetworkExceptionInfo(it)
        } ?: ErrorMessage.NetworkException

        _errorMessages.emit(message)
    }
}
