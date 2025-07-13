package di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import viewmodels.appsDetails.AppDetailsViewModel
import viewmodels.installedApps.InstalledAppsViewModel

val installedAppsPresentationModule = module {
    viewModel {
        InstalledAppsViewModel(
            getInstalledAppsUseCase = get()
        )
    }
    
    viewModel {
        AppDetailsViewModel(
            getAppDetailsUseCase = get()
        )
    }
}