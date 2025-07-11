package callbacks

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import entities.info.ConnectionType
import entities.info.NetworkInfo
import mappers.NetworkMapper

class NetworkCallbackImpl(
    private val networkMapper: NetworkMapper,
    private val connectivityManager: ConnectivityManager,
    private val onNetworkChanged: (NetworkInfo) -> Unit
) : ConnectivityManager.NetworkCallback() {
    
    override fun onAvailable(network: Network) {
        onNetworkChanged(networkMapper.mapNetworkInfo(network, connectivityManager))
    }
    
    override fun onLost(network: Network) {
        onNetworkChanged(NetworkInfo(ConnectionType.NONE, false, null))
    }
    
    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        onNetworkChanged(networkMapper.mapNetworkInfo(network, connectivityManager))
    }
}