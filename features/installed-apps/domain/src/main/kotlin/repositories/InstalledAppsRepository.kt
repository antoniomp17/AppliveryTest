package repositories

import entities.InstalledApp
import kotlinx.coroutines.flow.Flow

interface InstalledAppsRepository {
    suspend fun getInstalledApps(): List<InstalledApp>
    suspend fun getAppDetails(packageName: String): InstalledApp?
    fun observeAppInstallations(): Flow<List<InstalledApp>>
}