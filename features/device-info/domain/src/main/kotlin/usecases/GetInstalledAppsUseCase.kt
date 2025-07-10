package usecases

import entities.apps.InstalledApp
import repositories.InstalledAppsRepository

class GetInstalledAppsUseCase(
    private val installedAppsRepository: InstalledAppsRepository
) {
    suspend operator fun invoke(): List<InstalledApp> {
        return installedAppsRepository.getInstalledApps()
    }
}