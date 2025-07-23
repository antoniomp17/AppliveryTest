package callbacks

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import entities.LocationInfo
import mappers.LocationMapper

class LocationCallbackImpl(
    private val locationMapper: LocationMapper,
    private val onLocationChanged: (LocationInfo) -> Unit
): LocationCallback() {

    override fun onLocationResult(locationResult: LocationResult) {
        locationResult.lastLocation?.let { location ->
            val mappedLocation = locationMapper.mapLocationInfo(location)
            onLocationChanged(mappedLocation)
        }
    }
}