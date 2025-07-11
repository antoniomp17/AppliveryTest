package repositories

import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
import entities.MonitoringEvent
import entities.info.BatteryInfo
import entities.info.ConnectionType
import entities.info.DeviceInfo
import entities.info.NetworkInfo
import entities.info.StorageInfo
import exceptions.DeviceInfoException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive

class DeviceInfoRepositoryImpl(
    private val deviceBasicInfoDataSource: DeviceBasicInfoDataSource,
    private val batteryDataSource: BatteryDataSource,
    private val networkDataSource: NetworkDataSource,
    private val storageDataSource: StorageDataSource
) : DeviceInfoRepository {
    
    override suspend fun getDeviceInfo(): DeviceInfo {
        return try {
            DeviceInfo(
                deviceModel = deviceBasicInfoDataSource.getDeviceModel(),
                manufacturer = deviceBasicInfoDataSource.getManufacturer(),
                osVersion = deviceBasicInfoDataSource.getOsVersion(),
                availableStorage = storageDataSource.getStorageInfo(),
                batteryInfo = batteryDataSource.getBatteryInfo(),
                networkInfo = networkDataSource.getNetworkInfo()
            )
        } catch (e: Exception) {
            throw DeviceInfoException("Error getting device info: ${e.message}", e)
        }
    }
    
    override suspend fun getBatteryInfo(): BatteryInfo {
        return try {
            batteryDataSource.getBatteryInfo()
        } catch (e: Exception) {
            throw DeviceInfoException("Error getting battery info: ${e.message}", e)
        }
    }
    
    override suspend fun getStorageInfo(): StorageInfo {
        return try {
            storageDataSource.getStorageInfo()
        } catch (e: Exception) {
            throw DeviceInfoException("Error getting storage info: ${e.message}", e)
        }
    }
    
    override suspend fun getNetworkInfo(): NetworkInfo {
        return try {
            networkDataSource.getNetworkInfo()
        } catch (e: Exception) {
            NetworkInfo(ConnectionType.NONE, false, null)
        }
    }
    
    override fun observeMonitoringEvents(): Flow<MonitoringEvent> {
        return merge(
            batteryDataSource.observeBatteryChanges()
                .map { MonitoringEvent.BatteryChanged(it) },
            
            networkDataSource.observeNetworkChanges()
                .map { MonitoringEvent.NetworkChanged(it) },

            flow {
                while (currentCoroutineContext().isActive) {
                    delay(30_000)
                    try {
                        val storageInfo = storageDataSource.getStorageInfo()
                        emit(MonitoringEvent.StorageChanged(storageInfo))
                    } catch (e: Exception) { } // Ignore
                }
            }
        ).catch { e ->
            println("Error in monitoring events: ${e.message}")
        }
    }
}