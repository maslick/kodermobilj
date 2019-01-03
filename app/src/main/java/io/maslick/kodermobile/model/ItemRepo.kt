package io.maslick.kodermobile.model

import android.annotation.SuppressLint
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.rest.IBarkoderApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class ItemRepo(val api: IBarkoderApi, val dao: ItemDao) {

    fun getAllItems(header: String, view: ItemsContract.View): Observable<List<Item>> {
        return Observable.concatArrayEager(
            getItemsFromDb(),
            getItemsFromApi(header, view)
        )
    }

    private fun getItemsFromDb(): Observable<List<Item>> {
        return dao.getItems()
            .toObservable()
            .doOnNext { println("Dispatching ${it.size} items from DB...") }
    }

    @SuppressLint("CheckResult")
    private fun getItemsFromApi(header: String, view: ItemsContract.View): Observable<List<Item>> {
        return api.getAllItems(header)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                when(it.code()) {
                    401 -> view.showLoadingItemsError(": are you logged in?")
                    403 -> view.showLoadingItemsError(": access forbidden")
                    503 -> view.showLoadingItemsError(": service unavailable")
                }
            }
            .materialize()
            .map {
                it.error?.let { t ->
                    when {
                        UnknownHostException::class.isInstance(t) -> view.showLoadingItemsError(": are you offline?")
                        SocketTimeoutException::class.isInstance(t) -> view.showLoadingItemsError(": timeout, try again later")
                    }
                }
                it
            }
            .filter { !it.isOnError }
            .dematerialize { it }
            .debounce(400, TimeUnit.MILLISECONDS)
            .map { it.body() ?: emptyList() }
            .doOnNext {
                println("Dispatching ${it.size} items from API...")
                storeItemsInDb(it)
            }
    }

    @SuppressLint("CheckResult")
    private fun storeItemsInDb(items: List<Item>) {
        Observable.fromCallable { dao.insertAll(items) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                println("Inserted ${items.size} items from API to DB...")
            }
    }
}