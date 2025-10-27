package com.example.jetmap

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.jetmap.ui.MainActivityViewModel
import com.example.jetmap.ui.theme.JetMapTheme
import com.example.jetmap.ui.views.GoogleMapView
import com.example.jetmap.ui.views.PermissionRationaleDialog
import com.example.jetmap.ui.views.RationaleState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainActivityViewModel = hiltViewModel()
            val locationPermissionsState = rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            var rationaleState by remember { mutableStateOf<RationaleState?>(null) }
            var isUserLocationEnabled by remember { mutableStateOf(false) }
            var isMapLoaded by remember { mutableStateOf(false) }
            var hasZoomedToUser by remember { mutableStateOf(false) }
            val hasFine = locationPermissionsState.permissions
                .firstOrNull { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }
                ?.status?.isGranted == true
            val hasCoarse = locationPermissionsState.permissions
                .firstOrNull { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }
                ?.status?.isGranted == true
            val hasAnyLocationPermission = hasFine || hasCoarse
            val noLocationGranted = !hasCoarse && !hasFine

            // Request permissions on first run
            LaunchedEffect(Unit) {
                if (!hasAnyLocationPermission) {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            }

            LaunchedEffect(hasAnyLocationPermission, hasZoomedToUser) {
                if (hasAnyLocationPermission && !hasZoomedToUser) {
                    viewModel.requestInitialLocationZoom()
                    isUserLocationEnabled = true
                    hasZoomedToUser = true
                }
            }

            if (noLocationGranted && locationPermissionsState.shouldShowRationale) {
                rationaleState = RationaleState(
                    title = getString(R.string.rationale_request_location_access_title),
                    rationale = getString(R.string.rationale_request_location_access_description)
                ) { proceed ->
                    if (proceed) {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                    rationaleState = null
                }
            }

            JetMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Show rationale dialog when needed
                    rationaleState?.let {
                        PermissionRationaleDialog(it)
                    }

                    GoogleMapView(
                        viewModel = viewModel,
                        isUserLocationEnabled = isUserLocationEnabled,
                        isMapLoaded = isMapLoaded,
                        onMapLoaded = { isMapLoaded = true }
                    )
                }
            }
        }
    }
}