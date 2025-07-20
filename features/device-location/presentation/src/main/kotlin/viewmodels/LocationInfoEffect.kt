package viewmodels

sealed class LocationInfoEffect {
    data class ShowError(val message: String) : LocationInfoEffect()
    object ShowRefreshSuccess : LocationInfoEffect()
    data class ShowToast(val message: String) : LocationInfoEffect()
}