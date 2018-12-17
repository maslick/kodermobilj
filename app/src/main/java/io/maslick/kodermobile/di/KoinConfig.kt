package io.maslick.kodermobile.di

import android.content.Context
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
import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


val mvp = module {
    factory { ItemsFragment() }
    factory { ItemsPresenter(get("dev")) as ItemsContract.Presenter }

    factory { AddEditItemFragment() }
    factory { AddEditItemPresenter(getProperty(EDIT_ITEM_ID), get("dev"), getProperty(LOAD_DATA, true)) as AddEditItemContract.Presenter }
}

val sharedPrefsModule = module {
    fun prefs(context: Context) = context.getSharedPreferences("barkoder", Context.MODE_PRIVATE)!!
    single { prefs(get()) }
}

val keycloakApi = module {
    single { GsonBuilder().setLenient().create() }
    single {
        HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { mess -> println(mess) })
            .setLevel(HttpLoggingInterceptor.Level.BODY) as Interceptor
    }
    single {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(get())
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }
}

val barkoderApi = module {
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

interface IBarkoderApi {
    @GET("items")
    fun getAllItems(@Header("Authorization") header: String = ""): Observable<List<Item>>

    @GET("item/{id}")
    fun getItemWithId(@Path("id") id: Int, @Header("Authorization") header: String = ""): Observable<Item>

    @GET("barcode/{barcode}")
    fun getItemWithBarcode(@Path("barcode") barcode: String, @Header("Authorization") header: String = ""): Observable<Item>

    @POST("item")
    fun postItem(@Body item: Item, @Header("Authorization") header: String = ""): Observable<Response>

    @POST("items")
    fun postItems(@Body items: List<Item>, @Header("Authorization") header: String = ""): Observable<Response>

    @PUT("item")
    fun editItem(@Body item: Item, @Header("Authorization") header: String = ""): Observable<Response>

    @DELETE("item/{id}")
    fun deleteItemWithId(@Path("id") id: Int, @Header("Authorization") header: String = ""): Observable<Response>

    @DELETE("barcode/{barcode}")
    fun deleteItemWithBarcode(@Path("barcode") barcode: String, @Header("Authorization") header: String = ""): Observable<Response>
}

data class Item(
    var id: Int? = null,
    var title: String? = null,
    var category: String? = null,
    var description: String? = null,
    var barcode: String? = null,
    var quantity: Int? = null
)

enum class Status { ERROR, OK }
data class Response(val status: Status, val errorMessage: String? = null)