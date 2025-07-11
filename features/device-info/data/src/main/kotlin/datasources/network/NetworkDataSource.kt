package datasources.network

import entities.info.NetworkInfo
import kotlinx.coroutines.flow.Flow

interface NetworkDataSource {
    suspend fun getNetworkInfo(): NetworkInfo
    fun observeNetworkChanges(): Flow<NetworkInfo>
}