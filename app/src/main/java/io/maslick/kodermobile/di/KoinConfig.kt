package io.maslick.kodermobile.di

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.maslick.kodermobile.Config.barkoderBaseDevUrl
import io.maslick.kodermobile.Config.barkoderBaseProdUrl
import io.maslick.kodermobile.Config.baseUrl
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemContract
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemFragment
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemPresenter
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.mvp.listItems.ItemsFragment
import io.maslick.kodermobile.mvp.listItems.ItemsPresenter
import io.maslick.kodermobile.storage.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.storage.SharedPreferencesOAuth2Storage
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*
import java.util.concurrent.TimeUnit


val mvp = module {
    factory { ItemsFragment() }
    factory { ItemsPresenter(get("dev")) as ItemsContract.Presenter }

    factory { AddEditItemFragment() }
    factory { AddEditItemPresenter(get("dev")) as AddEditItemContract.Presenter }
}

val sharedPrefsModule = module {
    fun prefs(context: Context) = context.getSharedPreferences("barkoder", Context.MODE_PRIVATE)!!
    single { prefs(get()) }
    single<IOAuth2AccessTokenStorage> { SharedPreferencesOAuth2Storage(get(), get()) }
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
    single("keycloak.api") {
        Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(IKeycloakRest::class.java)
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

///////////////////////////////////////////
// Helper definitions
///////////////////////////////////////////

interface IKeycloakRest {
    @POST("token")
    @FormUrlEncoded
    fun grantNewAccessToken(
        @Field("code")         code: String,
        @Field("client_id")    clientId: String,
        @Field("redirect_uri") uri: String,
        @Field("grant_type")   grantType: String = "authorization_code"
    ): Observable<KeycloakToken>

    @POST("token")
    @FormUrlEncoded
    fun refreshAccessToken(
        @Field("refresh_token") refreshToken: String,
        @Field("client_id")     clientId: String,
        @Field("grant_type")    grantType: String = "refresh_token"
    ): Observable<KeycloakToken>

    @POST("logout")
    @FormUrlEncoded
    fun logout(
        @Field("client_id")     clientId: String,
        @Field("refresh_token") refreshToken: String
    ): Completable
}

data class KeycloakToken(
    @SerializedName("access_token")       var accessToken: String? = null,
    @SerializedName("expires_in")         var expiresIn: Int? = null,
    @SerializedName("refresh_expires_in") var refreshExpiresIn: Int? = null,
    @SerializedName("refresh_token")      var refreshToken: String? = null,
    @SerializedName("token_type")         var tokenType: String? = null,
    @SerializedName("id_token")           var idToken: String? = null,
    @SerializedName("not-before-policy")  var notBeforePolicy: Int? = null,
    @SerializedName("session_state")      var sessionState: String? = null,
    @SerializedName("expiration_date")    var expirationDate: Calendar? = null
)

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