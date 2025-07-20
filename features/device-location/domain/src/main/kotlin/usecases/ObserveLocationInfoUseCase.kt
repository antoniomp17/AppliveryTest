package usecases

import entities.LocationInfo
import kotlinx.coroutines.flow.Flow
import repositories.LocationRepository

class ObserveLocationInfoUseCase(private val locationRepository: LocationRepository) {
    operator fun invoke(): Flow<LocationInfo> {
        return locationRepository.observeLocationInfoUpdates()
    }
}