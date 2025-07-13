package di

import android.content.Context
import android.net.ConnectivityManager
import android.os.BatteryManager
import datasources.DeviceInfoDataSource
import datasources.DeviceInfoDataSourceImpl
import datasources.battery.BatteryDataSource
import datasources.battery.BatteryDataSourceImpl
import datasources.device.DeviceBasicInfoDataSource
import datasources.device.DeviceBasicInfoDataSourceImpl
import datasources.network.NetworkDataSource
import datasources.network.NetworkDataSourceImpl
import datasources.storage.StorageDataSource
import datasources.storage.StorageDataSourceImpl
import mappers.BatteryMapper
import mappers.NetworkMapper
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import repositories.DeviceInfoRepository
import repositories.DeviceInfoRepositoryImpl

val deviceInfoDataModule = module {

    // System Services
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    // Mappers
    single { BatteryMapper() }
    single { NetworkMapper() }

    // Specific DataSources
    single<DeviceBasicInfoDataSource> { DeviceBasicInfoDataSourceImpl() }
    single<BatteryDataSource> { BatteryDataSourceImpl(androidContext(), get()) }
    single<NetworkDataSource> { NetworkDataSourceImpl(androidContext(), get(), get()) }
    single<StorageDataSource> { StorageDataSourceImpl() }

    // Main DataSource
    single<DeviceInfoDataSource> {
        DeviceInfoDataSourceImpl(get(), get(), get(), get())
    }

    // Repository Implementation
    single<DeviceInfoRepository> {
        DeviceInfoRepositoryImpl(get(), get(), get(), get())
    }
}