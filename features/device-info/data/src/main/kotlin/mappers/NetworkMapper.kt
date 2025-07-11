package mappers

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import entities.info.ConnectionType
import entities.info.NetworkInfo

class NetworkMapper {
    @Suppress("MissingPermission")
    fun mapNetworkInfo(activeNetwork: Network?, connectivityManager: ConnectivityManager): NetworkInfo {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        return if (networkCapabilities != null) {
            val connectionType = when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                else -> ConnectionType.NONE
            }
            
            val signalStrength = when (connectionType) {
                ConnectionType.CELLULAR -> 85
                ConnectionType.WIFI -> 90
                else -> null
            }
            
            NetworkInfo(connectionType, true, signalStrength)
        } else {
            NetworkInfo(ConnectionType.NONE, false, null)
        }
    }
}