package viewmodels.appsDetails

sealed class AppDetailsIntent {
    data class LoadAppDetails(val packageName: String) : AppDetailsIntent()
    object ClearError : AppDetailsIntent()
}