package io.maslick.kodermobile.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.google.gson.GsonBuilder
import io.maslick.kodermobile.Config.barkoderBaseDevUrl
import io.maslick.kodermobile.Config.barkoderBaseProdUrl
import io.maslick.kodermobile.di.Properties.EDIT_ITEM_ID
import io.maslick.kodermobile.di.Properties.LOAD_DATA
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemContract
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemFragment
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemPresenter
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.mvp.listItems.ItemsFragment
import io.maslick.kodermobile.mvp.listItems.ItemsPresenter
import io.maslick.kodermobile.rest.IBarkoderApi
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


val mvp = module {
    factory { ItemsFragment() }
    factory { ItemsPresenter(get("dev")) as ItemsContract.Presenter }

    factory { AddEditItemFragment() }
    factory { AddEditItemPresenter(getProperty(EDIT_ITEM_ID), get("dev"), getProperty(LOAD_DATA, true)) as AddEditItemContract.Presenter }
}

val sharedPrefsModule = module {
    single { prefs(get()) }
}

val cache = module {
    single { cache(get()) }
    single("cacheInterceptor") {
        Interceptor { chain ->
            var request = chain.request()
            request = if(hasNetwork(get())!!)
                request.newBuilder().header("Cache-Control", "public, max-age=" + 15).build()
            else
                request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
            chain.proceed(request)
        }
    }
}

val barkoderApi = module {
    single { GsonBuilder().setLenient().create() }
    single("loggingInterceptor") {
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { mess -> println(mess) })
            .setLevel(HttpLoggingInterceptor.Level.BODY) as Interceptor
    }
    single {
        OkHttpClient()
            .newBuilder()
            .cache(get())
            .addInterceptor(get("loggingInterceptor"))
            .addInterceptor(get("cacheInterceptor"))
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }
    single("dev") {
        Retrofit.Builder()
            .baseUrl(barkoderBaseDevUrl)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(IBarkoderApi::class.java)
    }
    single("prod") {
        Retrofit.Builder()
            .baseUrl(barkoderBaseProdUrl)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(IBarkoderApi::class.java)
    }
}

object Properties {
    const val EDIT_ITEM_ID = "EDIT_ITEM_ID"
    const val LOAD_DATA = "EDIT_LOAD_DATA"
}

///////////////////////////////////////////
// Helper definitions
///////////////////////////////////////////

fun cache(context: Context): Cache {
    val file = File(context.cacheDir, "httpCache")
    file.mkdirs()
    return Cache(file, 5*1000*1000)
}

fun hasNetwork(context: Context): Boolean? {
    var isConnected: Boolean? = false // Initial Value
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    if (activeNetwork != null && activeNetwork.isConnected)
        isConnected = true
    return isConnected
}

fun prefs(context: Context) = context.getSharedPreferences("kodermobile", Context.MODE_PRIVATE)!!
