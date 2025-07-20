package viewmodels

sealed class LocationInfoIntent{
    object LoadLocationInfo : LocationInfoIntent()
    object RefreshLocationInfo : LocationInfoIntent()
    object ClearError : LocationInfoIntent()
    object RetryLoading : LocationInfoIntent()
    object RequestLocationPermission : LocationInfoIntent()
    data class OnPermissionResult(val granted: Boolean) : LocationInfoIntent()
}