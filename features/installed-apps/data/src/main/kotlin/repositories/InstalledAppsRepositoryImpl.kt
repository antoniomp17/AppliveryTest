package repositories

import datasources.InstalledAppsDataSource
import entities.InstalledApp
import exceptions.InstalledAppsException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class InstalledAppsRepositoryImpl(
    private val installedAppsDataSource: InstalledAppsDataSource
) : InstalledAppsRepository {
    
    override suspend fun getInstalledApps(): List<InstalledApp> {
        return try {
            installedAppsDataSource.getInstalledApps()
        } catch (e: Exception) {
            throw InstalledAppsException("Error getting installed apps: ${e.message}", e)
        }
    }
    
    override suspend fun getAppDetails(packageName: String): InstalledApp? {
        return try {
            installedAppsDataSource.getAppDetails(packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun observeAppInstallations(): Flow<List<InstalledApp>> {
        return installedAppsDataSource.observeAppInstallations()
            .catch { e ->
                emit(emptyList())
            }
    }
}