package mappers

import android.location.Location

class LocationMapper {
    fun mapLocationInfo(location: Location): entities.LocationInfo {
        return entities.LocationInfo(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            timestamp = location.time,
            bearing = location.bearing,
            provider = location.provider?: ""
        )
    }
}