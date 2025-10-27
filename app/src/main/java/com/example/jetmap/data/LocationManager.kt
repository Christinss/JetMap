package com.example.jetmap.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface LocationManager {
    suspend fun getLastAccurateLocation(): Location?
}

class AppLocationManager @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    @param:ApplicationContext private val context: Context
) : LocationManager {

    @SuppressLint("MissingPermission")
    override suspend fun getLastAccurateLocation(): Location? {
        if (!isGooglePlayServicesAvailable()) return null

        val hasFine = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (!hasFine && !hasCoarse) return null

        val priority = if (hasFine) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.getCurrentLocation(priority, null)
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return code == ConnectionResult.SUCCESS
    }
}