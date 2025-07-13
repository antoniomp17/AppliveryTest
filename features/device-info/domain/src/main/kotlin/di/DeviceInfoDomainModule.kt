package di

import org.koin.dsl.module
import usecases.GetDeviceInfoUseCase
import usecases.ObserveMonitoringEventsUseCase

val deviceInfoDomainModule = module {
    single { GetDeviceInfoUseCase(get()) }
    single { ObserveMonitoringEventsUseCase(get()) }
}