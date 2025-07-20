package viewmodels

import entities.LocationInfo

data class LocationInfoState(
    val locationInfo: LocationInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0,
    val isRefreshing: Boolean = false
)