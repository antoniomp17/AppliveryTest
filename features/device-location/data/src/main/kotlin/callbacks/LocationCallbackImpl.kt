package callbacks

import com.google.android.gms.location.LocationAvailability
import entities.LocationInfo
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import mappers.LocationMapper

class LocationCallbackImpl(
    private val locationMapper: LocationMapper,
    private val onLocationChanged: (LocationInfo) -> Unit
): LocationCallback() {

    override fun onLocationResult(locationResult: LocationResult) {
        onLocationChanged(
            if (locationResult.lastLocation == null) {
                LocationInfo.noLocation()
            } else {
                locationMapper.mapLocationInfo(locationResult.lastLocation!!)
            }
        )
    }

    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
        if (!locationAvailability.isLocationAvailable) {
            onLocationChanged(
                LocationInfo.noLocation()
            )
        }
    }
}