package di

import org.koin.dsl.module
import usecases.GetLocationInfoUseCase
import usecases.ObserveLocationInfoUseCase

val locationInfoDomainModule = module {
    single { GetLocationInfoUseCase(get()) }
    single { ObserveLocationInfoUseCase(get()) }
}