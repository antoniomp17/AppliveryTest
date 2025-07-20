package datasources

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import mappers.LocationMapper
import java.util.concurrent.Executors

class LocationDatasourceImpl(
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationMapper: LocationMapper
): LocationDatasource {

    @Suppress("MissingPermission")
    override fun getLocation(): LocationInfo {
        return try {
            if (!hasLocationPermission()) {
                throw SecurityException("Location permission not granted")
            }

            val lastLocation = fusedLocationProviderClient.lastLocation.result
                ?: throw IllegalStateException("Last location is not available")

            locationMapper.mapLocationInfo(lastLocation)
        } catch (e: Exception) {
            throw LocationInfoException("Failed to get location: ${e.message}", e)
        }
    }

    override fun observeLocation(): Flow<LocationInfo> {
        if (!hasLocationPermission()) {
            return flow {
                throw LocationInfoException("Location permission not granted")
            }
        }
        return observeLocationChangesWithPermission()
    }

    @Suppress("MissingPermission")
    private fun observeLocationChangesWithPermission(): Flow<LocationInfo> = callbackFlow {
        val callback = LocationCallbackImpl(locationMapper) { trySend(it) }
        val executor = Executors.newSingleThreadExecutor()

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setMinUpdateDistanceMeters(10f)
            setMaxUpdateDelayMillis(15000L)
        }.build()

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                executor,
                callback
            )

            val lastLocation = fusedLocationProviderClient.lastLocation.result
            if (lastLocation != null) {
                trySend(locationMapper.mapLocationInfo(lastLocation))
            }
        } catch (e: SecurityException) {
            close(LocationInfoException("Location permission revoked", e))
        } catch (e: Exception) {
            close(LocationInfoException("Failed to observe location updates", e))
        }

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(callback)
            executor.shutdown()
        }
    }.catch { e ->
        if (e is LocationInfoException) throw e
        throw LocationInfoException("Error in location flow", e)
    }.distinctUntilChanged()

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}