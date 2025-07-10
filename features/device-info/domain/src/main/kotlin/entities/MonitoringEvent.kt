package entities

import entities.info.BatteryInfo
import entities.info.NetworkInfo
import entities.info.StorageInfo

sealed class MonitoringEvent {
    data class BatteryChanged(
        val batteryInfo: BatteryInfo,
        val timestamp: Long = System.currentTimeMillis()
    ) : MonitoringEvent()
    
    data class NetworkChanged(
        val networkInfo: NetworkInfo,
        val timestamp: Long = System.currentTimeMillis()
    ) : MonitoringEvent()
    
    data class StorageChanged(
        val storageInfo: StorageInfo,
        val timestamp: Long = System.currentTimeMillis()
    ) : MonitoringEvent()
}