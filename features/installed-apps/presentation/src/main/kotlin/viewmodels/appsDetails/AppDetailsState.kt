package viewmodels.appsDetails

import entities.InstalledApp

data class AppDetailsState(
    val isLoading: Boolean = false,
    val app: InstalledApp? = null,
    val error: String? = null
)