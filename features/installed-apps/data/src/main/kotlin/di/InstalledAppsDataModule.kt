package di

import datasources.InstalledAppsDataSource
import datasources.InstalledAppsDataSourceImpl
import mappers.InstalledAppsMapper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import repositories.InstalledAppsRepository
import repositories.InstalledAppsRepositoryImpl

val installedAppsDataModule = module {

    single { androidContext().packageManager }

    // Mappers
    single { InstalledAppsMapper() }

    single<InstalledAppsDataSource> {
        InstalledAppsDataSourceImpl(
            packageManager = get(),
            installedAppsMapper = get()
        )
    }

    // Repository Implementation
    single<InstalledAppsRepository> {
        InstalledAppsRepositoryImpl(get())
    }
}