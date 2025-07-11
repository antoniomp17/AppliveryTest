package datasources

import entities.info.BatteryInfo
import entities.info.NetworkInfo
import entities.info.StorageInfo
import kotlinx.coroutines.flow.Flow

interface DeviceInfoDataSource {
    suspend fun getDeviceModel(): String
    suspend fun getManufacturer(): String
    suspend fun getOsVersion(): String
    suspend fun getBatteryInfo(): BatteryInfo
    suspend fun getStorageInfo(): StorageInfo
    suspend fun getNetworkInfo(): NetworkInfo
    fun observeBatteryChanges(): Flow<BatteryInfo>
    fun observeNetworkChanges(): Flow<NetworkInfo>
}