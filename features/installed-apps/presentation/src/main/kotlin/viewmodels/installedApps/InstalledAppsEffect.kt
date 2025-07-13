package viewmodels.installedApps

sealed class InstalledAppsEffect {
    data class ShowError(val message: String) : InstalledAppsEffect()
    object ShowRefreshSuccess : InstalledAppsEffect()
    data class NavigateToAppDetails(val packageName: String) : InstalledAppsEffect()
    data class ShowToast(val message: String) : InstalledAppsEffect()
}