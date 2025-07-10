package di

import org.koin.dsl.module
import usecases.GetDeviceInfoUseCase
import usecases.GetInstalledAppsUseCase
import usecases.ObserveMonitoringEventsUseCase

val domainModule = module {
    single { GetDeviceInfoUseCase(get()) }
    single { GetInstalledAppsUseCase(get()) }
    single { ObserveMonitoringEventsUseCase(get()) }
}