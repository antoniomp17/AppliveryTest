package datasources.device

interface DeviceBasicInfoDataSource {
    suspend fun getDeviceModel(): String
    suspend fun getManufacturer(): String
    suspend fun getOsVersion(): String
}