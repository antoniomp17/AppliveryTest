package datasources

import entities.InstalledApp
import kotlinx.coroutines.flow.Flow

interface InstalledAppsDataSource {
    suspend fun getInstalledApps(): List<InstalledApp>
    suspend fun getAppDetails(packageName: String): InstalledApp?
    fun observeAppInstallations(): Flow<List<InstalledApp>>
}