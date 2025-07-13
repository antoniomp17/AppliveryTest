package com.amp.appliverytest

import android.app.Application
import com.amp.appliverytest.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AppliveryApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@AppliveryApplication)
            modules(appModule)
        }
    }
}