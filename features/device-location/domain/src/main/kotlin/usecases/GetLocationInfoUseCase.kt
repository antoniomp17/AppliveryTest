package usecases

import entities.LocationInfo
import repositories.LocationRepository

class GetLocationInfoUseCase(private val locationRepository: LocationRepository) {
    suspend operator fun invoke(): LocationInfo {
        return locationRepository.getLocationInfo()
    }
}