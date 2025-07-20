package di

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import datasources.LocationDatasource
import datasources.LocationDatasourceImpl
import mappers.LocationMapper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import repositories.LocationRepository
import repositories.LocationRepositoryImpl

val locationInfoDataModule = module {

    // System services
    single<FusedLocationProviderClient> {
        LocationServices.getFusedLocationProviderClient(androidContext())
    }

    // Mappers
    single { LocationMapper() }

    // Datasources
    single<LocationDatasource> {
        LocationDatasourceImpl(
            androidContext(),
            get(),
            get()
        )
    }

    // Repository Implementation
    single<LocationRepository> { LocationRepositoryImpl(get()) }
}