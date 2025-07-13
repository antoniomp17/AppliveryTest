package viewmodels.appsDetails

sealed class AppDetailsEffect {
    data class ShowError(val message: String) : AppDetailsEffect()
    object NavigateBack : AppDetailsEffect()
}