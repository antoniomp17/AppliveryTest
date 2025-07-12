package usecases

import entities.InstalledApp
import repositories.InstalledAppsRepository

class GetAppDetailsUseCase(
    private val installedAppsRepository: InstalledAppsRepository
) {
    suspend operator fun invoke(packageName: String): InstalledApp? {
        return installedAppsRepository.getAppDetails(packageName)
    }
}