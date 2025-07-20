package repositories

import entities.LocationInfo
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getLocationInfo(): LocationInfo
    fun observeLocationInfoUpdates(): Flow<LocationInfo>
}