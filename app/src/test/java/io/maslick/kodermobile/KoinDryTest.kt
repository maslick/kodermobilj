package io.maslick.kodermobile

import io.maslick.kodermobile.di.barkoderApi
import io.maslick.kodermobile.di.keycloakApi
import io.maslick.kodermobile.di.mvp
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.checkModules

class KoinDryTest : KoinTest {

    @Test
    fun itemsModuleDryRunTest() {
        checkModules(listOf(mvp, barkoderApi, keycloakApi))
    }
}