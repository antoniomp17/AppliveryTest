package datasources

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import entities.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import mappers.InstalledAppsMapper

class InstalledAppsDataSourceImpl(
    private val packageManager: PackageManager,
    private val installedAppsMapper: InstalledAppsMapper
) : InstalledAppsDataSource {

    override suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        try {
            val packageInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(0)
            }

            packageInfoList
                .filter { packageInfo ->
                    (packageInfo.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) == 0
                }
                .mapNotNull { packageInfo ->
                    installedAppsMapper.mapToInstalledApp(packageInfo, packageManager)
                }
                .sortedBy { it.name.lowercase() }

        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAppDetails(packageName: String): InstalledApp? =
        withContext(Dispatchers.IO) {
            try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, 0)
                }

                installedAppsMapper.mapToInstalledApp(packageInfo, packageManager)
            } catch (e: Exception) {
                null
            }
        }

    override fun observeAppInstallations(): Flow<List<InstalledApp>> = flow {

        emit(getInstalledApps())

    }.flowOn(Dispatchers.IO)
}