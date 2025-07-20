package datasources

import entities.LocationInfo
import kotlinx.coroutines.flow.Flow

interface LocationDatasource {
    suspend fun getLocation(): LocationInfo
    fun observeLocation(): Flow<LocationInfo>
}