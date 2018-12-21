package io.maslick.kodermobile

import android.content.Context
import io.maslick.kodermobile.di.kodermobileModules
import org.junit.Test
import org.koin.dsl.module.module
import org.koin.test.KoinTest
import org.koin.test.checkModules
import org.mockito.Mockito.mock

class KoinDryTest : KoinTest {

    private val androidStuff = module {
        single { mock(Context::class.java) }
    }

    @Test
    fun itemsModuleDryRunTest() {
        checkModules(kodermobileModules + androidStuff)
    }
}