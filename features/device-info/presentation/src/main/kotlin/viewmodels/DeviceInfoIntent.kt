package viewmodels

sealed class DeviceInfoIntent {
    object LoadDeviceInfo : DeviceInfoIntent()
    object RefreshDeviceInfo : DeviceInfoIntent()
    object ClearError : DeviceInfoIntent()
    object RetryLoading : DeviceInfoIntent()
}