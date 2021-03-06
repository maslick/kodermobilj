package io.maslick.kodermobile.rest

import io.maslick.kodermobile.model.Item
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface IBarkoderApi {
    @GET("items")
    fun getAllItems(@Header("Authorization") header: String = ""): Observable<Response<List<Item>>>

    @GET("item/{id}")
    fun getItemWithId(@Path("id") id: Int, @Header("Authorization") header: String = ""): Observable<Response<Item>>

    @GET("barcode/{barcode}")
    fun getItemWithBarcode(@Path("barcode") barcode: String, @Header("Authorization") header: String = ""): Observable<Item>

    @POST("item")
    fun postItem(@Body item: Item, @Header("Authorization") header: String = ""): Observable<Response<Resp>>

    @POST("items")
    fun postItems(@Body items: List<Item>, @Header("Authorization") header: String = ""): Observable<Resp>

    @PUT("item")
    fun editItem(@Body item: Item, @Header("Authorization") header: String = ""): Observable<Response<Resp>>

    @DELETE("item/{id}")
    fun deleteItemWithId(@Path("id") id: Int, @Header("Authorization") header: String = ""): Observable<Response<Resp>>

    @DELETE("barcode/{barcode}")
    fun deleteItemWithBarcode(@Path("barcode") barcode: String, @Header("Authorization") header: String = ""): Observable<Resp>
}

enum class Status { ERROR, NETWORK_ERROR, OK }
data class Resp(val status: Status, val errorMessage: String? = null)