package io.maslick.kodermobile

import com.google.gson.GsonBuilder
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.model.Item
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RestApiTest {

    companion object {
        @JvmStatic
        lateinit var api: IBarkoderApi

        @BeforeClass
        @JvmStatic
        fun setup() {
            val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { mess -> println(mess) })
                .setLevel(HttpLoggingInterceptor.Level.BODY)

            val okHttpClient = OkHttpClient()
                .newBuilder()
                .addInterceptor(interceptor)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()


            val retrofit = Retrofit.Builder()
                .baseUrl("https://barkoder-dev.herokuapp.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

            api = retrofit.create(IBarkoderApi::class.java)
        }
    }

    @Test
    @Ignore
    fun testGetAllItems() {
        val latch = CountDownLatch(1)
        api.getAllItems().subscribe {
            it.forEach { item -> println(item) }
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }

    @Test
    @Ignore
    fun getOneItem() {
        val latch = CountDownLatch(1)
        api.getItemWithId(1).subscribe {
            println(it)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }

    @Test
    @Ignore
    fun getOneItemWithBarcode() {
        val latch = CountDownLatch(1)
        api.getItemWithBarcode("59012341234527").subscribe {
            println(it)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }

    @Test
    @Ignore
    fun postItem() {
        val latch = CountDownLatch(1)
        api.postItem(Item(title = "helloworld", category = "nothing", description = "none", barcode = "12345", quantity = 3)).subscribe {
            println(it)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }

    @Test
    @Ignore
    fun postItems() {
        val latch = CountDownLatch(1)
        val item1 = Item(title = "item1", category = "nothing", description = "none", barcode = "1", quantity = 1)
        val item2 = Item(title = "item2", category = "nothing", description = "none", barcode = "2", quantity = 1)
        val item3 = Item(title = "item3", category = "nothing", description = "none", barcode = "3", quantity = 1)
        api.postItems(listOf(item1, item2, item3)).subscribe {
            println(it)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }

    @Test
    @Ignore
    fun editItem() {
        val latch = CountDownLatch(1)
        api.editItem(Item(id = 5, title = "item1", category = "nothing", description = "none", barcode = "2", quantity = 123)).subscribe {
            println(it)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }

    @Test
    @Ignore
    fun deleteItemById() {
        val latch = CountDownLatch(1)
        api.deleteItemWithId(5).subscribe {
            println(it)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }

    @Test
    @Ignore
    fun deleteItemByBarcode() {
        val latch = CountDownLatch(1)
        api.deleteItemWithBarcode("12345").subscribe {
            println(it)
            latch.countDown()
        }
        latch.await(30, TimeUnit.SECONDS)
    }
}
