package di

import org.koin.dsl.module
import usecases.GetAppDetailsUseCase
import usecases.GetInstalledAppsUseCase

val installedAppsDomainModule = module {
    single { GetInstalledAppsUseCase(get()) }
    single { GetAppDetailsUseCase(get()) }
}