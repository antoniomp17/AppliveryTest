package repositories

import datasources.LocationDatasource
import entities.LocationInfo
import exceptions.LocationInfoException
import kotlinx.coroutines.flow.Flow

class LocationRepositoryImpl(
    private val locationDatasource: LocationDatasource
): LocationRepository {

    override suspend fun getLocationInfo(): LocationInfo {
        try {
            return locationDatasource.getLocation()
        } catch (e: Exception) {
            throw LocationInfoException("Error getting location info: ${e.message}", e)
        }
    }

    override fun observeLocationInfoUpdates(): Flow<LocationInfo> {
        try {
            return locationDatasource.observeLocation()
        } catch (e: Exception) {
            throw LocationInfoException("Error getting location info: ${e.message}", e)
        }
    }
}