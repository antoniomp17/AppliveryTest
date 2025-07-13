package di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import viewmodels.DeviceInfoViewModel

val deviceInfoPresentationModule = module {
    viewModel {
        DeviceInfoViewModel(
            getDeviceInfoUseCase = get(),
            observeMonitoringEventsUseCase = get()
        )
    }
}