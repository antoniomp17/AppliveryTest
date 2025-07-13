package viewmodels.installedApps

import entities.InstalledApp

sealed class InstalledAppsIntent {
    object LoadApps : InstalledAppsIntent()
    object RefreshApps : InstalledAppsIntent()
    data class SearchApps(val query: String) : InstalledAppsIntent()
    data class SelectApp(val app: InstalledApp) : InstalledAppsIntent()
    object ClearSelection : InstalledAppsIntent()
    object ClearError : InstalledAppsIntent()
    object ClearSearch : InstalledAppsIntent()
}