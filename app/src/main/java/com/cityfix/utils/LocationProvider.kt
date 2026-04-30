package com.cityfix.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LatLng(val latitude: Double, val longitude: Double)

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun isLocationEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): LatLng? {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        var bestLocation: Location? = null
        for (provider in providers) {
            val location = try {
                locationManager.getLastKnownLocation(provider)
            } catch (e: Exception) {
                null
            }
            if (location != null) {
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
        }
        return bestLocation?.let { LatLng(it.latitude, it.longitude) }
    }
}
