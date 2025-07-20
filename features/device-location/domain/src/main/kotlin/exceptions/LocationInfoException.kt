package exceptions

class LocationInfoException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)