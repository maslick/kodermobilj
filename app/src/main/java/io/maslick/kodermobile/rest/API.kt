package io.maslick.kodermobile.rest

import io.reactivex.Observable
import retrofit2.http.*

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