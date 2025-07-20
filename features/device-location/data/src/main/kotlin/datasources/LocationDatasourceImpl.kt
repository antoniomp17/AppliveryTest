package datasources

import android.content.Context
import androidx.core.content.ContextCompat
import callbacks.LocationCallbackImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import entities.LocationInfo
import exceptions.LocationInfoException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import mappers.LocationMapper
import java.util.concurrent.Executors

class LocationDatasourceImpl(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationMapper: LocationMapper
): LocationDatasource {

    // Shared executor that won't be shutdown during individual flow operations
    private val locationExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "LocationUpdates").apply {
            isDaemon = true
        }
    }

    @Suppress("MissingPermission")
    override suspend fun getLocation(): LocationInfo {
        return try {
            if (!hasLocationPermission()) {
                throw LocationInfoException("Location permission not granted")
            }

            suspendCancellableCoroutine { continuation ->
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location ->
                        val locationInfo = location?.let { 
                            locationMapper.mapLocationInfo(it) 
                        } ?: LocationInfo.noLocation()
                        continuation.resume(locationInfo)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(
                            LocationInfoException("Failed to get location", exception)
                        )
                    }
            }
        } catch (e: Exception) {
            if (e is LocationInfoException) throw e
            throw LocationInfoException("Error getting location", e)
        }
    }

    override fun observeLocation(): Flow<LocationInfo> {
        if (!hasLocationPermission()) {
            return flow {
                throw LocationInfoException("Location permission not granted")
            }
        }
        return observeLocationChanges()
    }

    private fun observeLocationChanges(): Flow<LocationInfo> {
        return observeLocationChangesWithPermission()
    }

    @Suppress("MissingPermission")
    private fun observeLocationChangesWithPermission(): Flow<LocationInfo> = callbackFlow {
        val callback = LocationCallbackImpl(locationMapper) { locationInfo ->
            trySend(locationInfo)
        }

        val locationRequest = createLocationRequest()

        try {
            requestLocationUpdates(locationRequest, callback)
            
            // Get last known location immediately
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val locationInfo = locationMapper.mapLocationInfo(it)
                        trySend(locationInfo)
                    }
                }
                .addOnFailureListener {
                    // Ignore failure, we'll get updates through callback
                }
        } catch (e: SecurityException) {
            close(LocationInfoException("Location permission revoked", e))
        } catch (e: Exception) {
            close(LocationInfoException("Failed to observe location updates", e))
        }

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(callback)
        }
    }.catch { e ->
        if (e is LocationInfoException) throw e
        throw LocationInfoException("Error in location flow", e)
    }.distinctUntilChanged()

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            30000L
        ).apply {
            setMinUpdateIntervalMillis(15000L)
            setMaxUpdateDelayMillis(45000L)
        }.build()
    }

    @Suppress("MissingPermission")
    private fun requestLocationUpdates(
        locationRequest: LocationRequest, 
        callback: LocationCallbackImpl
    ) {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationExecutor,
            callback
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}