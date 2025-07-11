package datasources.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.ContextCompat
import callbacks.NetworkCallbackImpl
import entities.info.ConnectionType
import entities.info.NetworkInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import mappers.NetworkMapper

class NetworkDataSourceImpl(
    private val context: Context,
    private val connectivityManager: ConnectivityManager,
    private val networkMapper: NetworkMapper
) : NetworkDataSource {

    @Suppress("MissingPermission")
    override suspend fun getNetworkInfo(): NetworkInfo {
        return if (hasNetworkPermission()) {
            networkMapper.mapNetworkInfo(connectivityManager.activeNetwork, connectivityManager)
        } else {
            NetworkInfo(ConnectionType.NONE, false, null)
        }
    }

    override fun observeNetworkChanges(): Flow<NetworkInfo> {
        return if (hasNetworkPermission()) {
            observeNetworkChangesWithPermission()
        } else {
            flowOf(NetworkInfo(ConnectionType.NONE, false, null))
        }
    }

    private fun hasNetworkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("MissingPermission")
    private fun observeNetworkChangesWithPermission(): Flow<NetworkInfo> = callbackFlow {
        val callback = NetworkCallbackImpl(networkMapper, connectivityManager) { trySend(it) }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(callback)
            } else {
                val builder = NetworkRequest.Builder()
                connectivityManager.registerNetworkCallback(builder.build(), callback)
            }
            trySend(
                networkMapper.mapNetworkInfo(
                    connectivityManager.activeNetwork,
                    connectivityManager
                )
            )
        } catch (e: SecurityException) {
            trySend(NetworkInfo(ConnectionType.NONE, false, null))
        }

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}