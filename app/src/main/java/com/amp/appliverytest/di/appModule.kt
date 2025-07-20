package com.amp.appliverytest.di

import di.deviceInfoDataModule
import di.deviceInfoDomainModule
import di.deviceInfoPresentationModule
import di.installedAppsDataModule
import di.installedAppsDomainModule
import di.installedAppsPresentationModule
import di.locationInfoDataModule
import di.locationInfoDomainModule
import org.koin.dsl.module

val appModule = module {
    includes(
        installedAppsDataModule,
        installedAppsDomainModule,
        installedAppsPresentationModule,
        deviceInfoDataModule,
        deviceInfoDomainModule,
        deviceInfoPresentationModule,
        locationInfoDomainModule,
        locationInfoDataModule
    )
}