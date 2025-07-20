package entities

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speed: Float,
    val bearing: Float,
    val provider: String,
    val timestamp: Long
) {
    override fun toString(): String {
        return "LocationInfo(latitude=$latitude, longitude=$longitude, altitude=$altitude, accuracy=$accuracy, speed=$speed, bearing=$bearing, provider='$provider', timestamp=$timestamp)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocationInfo

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (altitude != other.altitude) return false
        if (accuracy != other.accuracy) return false
        if (speed != other.speed) return false
        if (bearing != other.bearing) return false
        if (provider != other.provider) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + altitude.hashCode()
        result = 31 * result + accuracy.hashCode()
        result = 31 * result + speed.hashCode()
        result = 31 * result + bearing.hashCode()
        result = 31 * result + provider.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }

    companion object {
        fun noLocation(): LocationInfo {
            return LocationInfo(0.0, 0.0, 0.0, 0.0f, 0.0f, 0.0f, "", 0)
        }
    }
}
