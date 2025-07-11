package exceptions

class DeviceInfoException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)