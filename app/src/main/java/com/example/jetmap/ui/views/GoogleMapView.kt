package com.example.jetmap.ui.views

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jetmap.R
import com.example.jetmap.data.network.ParkingSpot
import com.example.jetmap.ui.ErrorMessage
import com.example.jetmap.ui.MainActivityViewModel
import com.example.jetmap.utils.AppConstants
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel,
    isUserLocationEnabled: Boolean,
    isMapLoaded: Boolean,
    onMapLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    val systemInsets = WindowInsets.systemBars
    val topPadding = systemInsets.asPaddingValues().calculateTopPadding()
    val bottomPadding = systemInsets.asPaddingValues().calculateBottomPadding()
    val parkingSpots by viewModel.parkingSpotsState.collectAsStateWithLifecycle()
    val selectedParkingSpot by viewModel.parkingSpotState.collectAsStateWithLifecycle()
    val loadingState by viewModel.loadingState.collectAsStateWithLifecycle()
    var allParkingSpots by rememberSaveable { mutableStateOf<List<ParkingSpot>>(emptyList()) }
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    var clusterToZoom by rememberSaveable { mutableStateOf<Cluster<ParkingSpot>?>(null) }
    var wasCameraMoving by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState()
    val scope = rememberCoroutineScope()
    val isLoading = !isMapLoaded || loadingState
    val loadingMessage = if (!isMapLoaded) stringResource(R.string.loading_map) else stringResource(R.string.loading_parking_spots)

    // Ensure Maps SDK is initialized
    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context)

        val availabilityCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        Timber.d("Play Services availability code: $availabilityCode")
        // If not SUCCESS, show an error dialog the user can act on
        if (availabilityCode != ConnectionResult.SUCCESS) {
            (context as? android.app.Activity)?.let { activity ->
                GoogleApiAvailability.getInstance()
                    .getErrorDialog(
                        activity,
                        availabilityCode,
                        AppConstants.Map.GOOGLE_PLAY_SERVICES_ERROR_DIALOG_REQUEST_CODE)
                    ?.show()
            }
        }
    }

    // Get user current location on app start
    LaunchedEffect(viewModel.zoomTrigger) {
        viewModel.zoomTrigger
            .onEach { latLng ->
                Timber.d("zoomTrigger received: $latLng")
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, AppConstants.Map.USER_ZOOM_LEVEL)
                cameraPositionState.animate(cameraUpdate, AppConstants.Map.ANIMATION_DURATION_MS)
            }
            .launchIn(scope)
    }

    LaunchedEffect(parkingSpots) {
        if (parkingSpots.isNotEmpty()) {
            Timber.d("Parking spots updated: ${parkingSpots.size} items")
            parkingSpots.forEach { spot ->
                Timber.d("Spot: ${spot.name} at ${spot.parkingSpotLocation.latitude}, ${spot.parkingSpotLocation.longitude}")
            }
            allParkingSpots = parkingSpots
        }
    }

    // Handle cluster zoom animation
    LaunchedEffect(clusterToZoom) {
        clusterToZoom?.let { cluster ->
            val bounds = LatLngBounds.builder()
            cluster.items.forEach { item ->
                bounds.include(LatLng(item.parkingSpotLocation.latitude, item.parkingSpotLocation.longitude))
            }
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), AppConstants.Map.CLUSTER_ZOOM_PADDING)
            cameraPositionState.animate(cameraUpdate)
            clusterToZoom = null // Reset after animation
        }
    }

    LaunchedEffect(cameraPositionState.isMoving, openBottomSheet) {
        // Only update if:
        // 1. Camera was moving and now stopped
        // 2. Bottom sheet is not open
        if (wasCameraMoving && !cameraPositionState.isMoving && !openBottomSheet) {
            cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { latLngBounds ->
                viewModel.getMapBounds(
                    latLngBounds.northeast.latitude,
                    latLngBounds.northeast.longitude,
                    latLngBounds.southwest.latitude,
                    latLngBounds.southwest.longitude
                )
            }
        }
        wasCameraMoving = cameraPositionState.isMoving
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessages.collect { error ->
            val message = when (error) {
                is ErrorMessage.LocationFailed -> context.getString(R.string.error_location_failed)
                is ErrorMessage.NetworkErrorWithMessage -> context.getString(R.string.error_network_message, error.code, error.message)
                is ErrorMessage.NetworkError -> context.getString(R.string.error_network_generic)
                is ErrorMessage.NetworkExceptionWithMessage -> context.getString(R.string.exception_network_message, error.message)
                is ErrorMessage.NetworkException -> context.getString(R.string.exception_network_generic)
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = isUserLocationEnabled
            ),
            onMapLoaded = {
                onMapLoaded()
                cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { latLngBounds ->
                    viewModel.getMapBounds(
                        latLngBounds.northeast.latitude,
                        latLngBounds.northeast.longitude,
                        latLngBounds.southwest.latitude,
                        latLngBounds.southwest.longitude
                    )
                }
            },
            contentPadding = PaddingValues(top = topPadding)
        ) {
            Clustering(
                items = allParkingSpots,
                onClusterItemClick = { parkingSpot ->
                    openBottomSheet = !openBottomSheet
                    viewModel.selectParkingSpot(parkingSpot)
                    true
                },
                onClusterClick = { cluster ->
                    clusterToZoom = cluster
                    true
                },
            )
        }

        AnimatedVisibility(
            visible = isLoading,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomPadding),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            )
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = loadingMessage,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    selectedParkingSpot?.let {
        val uri = "geo:${it.parkingSpotLocation.latitude},${it.parkingSpotLocation.longitude}" +
                "?q=${it.parkingSpotLocation.latitude},${it.parkingSpotLocation.longitude}"
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())


        ParkingSpotDetailsView(
            openBottomSheet = { toggleSheetState ->
                openBottomSheet = toggleSheetState
                if (!openBottomSheet) {
                    viewModel.clearSelectedParkingSpot()
                }
            },
            isBottomSheetOpen = openBottomSheet,
            isUserLocationEnabled = isUserLocationEnabled,
            parkingSpot = it
        ) {
            context.startActivity(intent)
        }

    }
}