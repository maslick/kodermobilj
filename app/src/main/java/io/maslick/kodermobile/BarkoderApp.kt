package io.maslick.kodermobile

import android.app.Application
import io.maslick.kodermobile.di.kodermobileModules
import io.maslick.kodermobile.oauth.RefreshTokenWorker
import org.koin.android.ext.android.startKoin


class BarkoderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, kodermobileModules)
        RefreshTokenWorker.startPeriodicRefreshTokenTask()
    }
}