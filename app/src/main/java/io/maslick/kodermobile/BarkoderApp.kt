package io.maslick.kodermobile

import android.app.Application
import io.maslick.kodermobile.di.barkoderApi
import io.maslick.kodermobile.di.keycloakApi
import io.maslick.kodermobile.di.mvp
import org.koin.android.ext.android.startKoin


class BarkoderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(mvp, barkoderApi, keycloakApi))
    }
}