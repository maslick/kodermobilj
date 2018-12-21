package io.maslick.kodermobile

import android.app.Application
import io.maslick.kodermobile.di.kodermobileModules
import org.koin.android.ext.android.startKoin


class BarkoderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, kodermobileModules)
    }
}