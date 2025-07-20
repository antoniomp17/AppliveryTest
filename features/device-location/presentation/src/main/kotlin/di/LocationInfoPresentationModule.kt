package di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import viewmodels.LocationInfoViewModel

val locationInfoPresentationModule = module {
    viewModel {
        LocationInfoViewModel(
            getLocationInfoUseCase = get(),
            observeLocationInfoUseCase = get()
        )
    }
}