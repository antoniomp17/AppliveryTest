package entities.info

data class DeviceInfo(
    val deviceModel: String,
    val manufacturer: String,
    val osVersion: String,
    val availableStorage: StorageInfo,
    val batteryInfo: BatteryInfo,
    val networkInfo: NetworkInfo
)