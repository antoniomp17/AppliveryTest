package datasources

import datasources.battery.BatteryDataSource
import datasources.device.DeviceBasicInfoDataSource
import datasources.network.NetworkDataSource
import datasources.storage.StorageDataSource
import entities.info.BatteryInfo
import entities.info.NetworkInfo
import entities.info.StorageInfo
import kotlinx.coroutines.flow.Flow

class DeviceInfoDataSourceImpl(
    private val deviceBasicInfoDataSource: DeviceBasicInfoDataSource,
    private val batteryDataSource: BatteryDataSource,
    private val networkDataSource: NetworkDataSource,
    private val storageDataSource: StorageDataSource
) : DeviceInfoDataSource {
    
    override suspend fun getDeviceModel(): String = deviceBasicInfoDataSource.getDeviceModel()
    override suspend fun getManufacturer(): String = deviceBasicInfoDataSource.getManufacturer()
    override suspend fun getOsVersion(): String = deviceBasicInfoDataSource.getOsVersion()
    
    override suspend fun getBatteryInfo(): BatteryInfo = batteryDataSource.getBatteryInfo()
    override fun observeBatteryChanges(): Flow<BatteryInfo> = batteryDataSource.observeBatteryChanges()
    
    override suspend fun getNetworkInfo(): NetworkInfo = networkDataSource.getNetworkInfo()
    override fun observeNetworkChanges(): Flow<NetworkInfo> = networkDataSource.observeNetworkChanges()
    
    override suspend fun getStorageInfo(): StorageInfo = storageDataSource.getStorageInfo()
}