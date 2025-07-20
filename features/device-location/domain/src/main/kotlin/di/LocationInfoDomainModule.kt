package di

import org.koin.dsl.module
import usecases.GetLocationInfoUseCase
import usecases.ObserverLocationInfoUseCase

val locationInfoDomainModule = module {
    single { GetLocationInfoUseCase(get()) }
    single { ObserverLocationInfoUseCase(get()) }
}