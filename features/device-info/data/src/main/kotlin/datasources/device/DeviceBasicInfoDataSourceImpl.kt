package datasources.device

import android.os.Build

class DeviceBasicInfoDataSourceImpl : DeviceBasicInfoDataSource {
    override suspend fun getDeviceModel(): String = Build.MODEL
    override suspend fun getManufacturer(): String = Build.MANUFACTURER
    override suspend fun getOsVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}