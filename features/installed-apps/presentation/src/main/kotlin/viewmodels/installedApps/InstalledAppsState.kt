package viewmodels.installedApps

import entities.InstalledApp

data class InstalledAppsState(
    val isLoading: Boolean = false,
    val apps: List<InstalledApp> = emptyList(),
    val filteredApps: List<InstalledApp> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val selectedApp: InstalledApp? = null,
    val totalAppsCount: Int = 0
)