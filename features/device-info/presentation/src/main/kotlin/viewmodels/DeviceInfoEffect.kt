package viewmodels

sealed class DeviceInfoEffect {
    data class ShowError(val message: String) : DeviceInfoEffect()
    object ShowRefreshSuccess : DeviceInfoEffect()
    data class ShowToast(val message: String) : DeviceInfoEffect()
}