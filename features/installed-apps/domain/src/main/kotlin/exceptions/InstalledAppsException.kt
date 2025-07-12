package exceptions

class InstalledAppsException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)