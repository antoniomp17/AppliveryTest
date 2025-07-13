package viewmodels

import entities.info.DeviceInfo

data class DeviceInfoState(
    val isLoading: Boolean = false,
    val deviceInfo: DeviceInfo? = null,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val lastUpdated: Long = 0L
)