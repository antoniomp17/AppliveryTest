package di

import datasources.InstalledAppsDataSource
import datasources.InstalledAppsDataSourceImpl
import mappers.InstalledAppsMapper
import org.koin.dsl.module
import repositories.InstalledAppsRepository
import repositories.InstalledAppsRepositoryImpl

val dataModule = module {

    single { InstalledAppsMapper() }

    single<InstalledAppsDataSource> {
        InstalledAppsDataSourceImpl(get(), get())
    }

    single<InstalledAppsRepository> {
        InstalledAppsRepositoryImpl(get())
    }
}