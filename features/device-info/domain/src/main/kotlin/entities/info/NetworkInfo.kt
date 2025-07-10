package entities.info

data class NetworkInfo(
    val connectionType: ConnectionType,
    val isConnected: Boolean,
    val signalStrength: Int? = null
)