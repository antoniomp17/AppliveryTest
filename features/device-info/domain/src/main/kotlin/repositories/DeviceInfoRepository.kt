package repositories

import entities.MonitoringEvent
import entities.info.BatteryInfo
import entities.info.DeviceInfo
import entities.info.NetworkInfo
import entities.info.StorageInfo
import kotlinx.coroutines.flow.Flow

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo
    suspend fun getBatteryInfo(): BatteryInfo
    suspend fun getStorageInfo(): StorageInfo
    suspend fun getNetworkInfo(): NetworkInfo
    fun observeMonitoringEvents(): Flow<MonitoringEvent>
}