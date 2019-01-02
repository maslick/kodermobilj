package io.maslick.kodermobile.model

import android.annotation.SuppressLint
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.rest.IBarkoderApi
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ItemRepo(val api: IBarkoderApi, val dao: ItemDao, val view: ItemsContract.View) {

    fun getAllItems(header: String): Observable<List<Item>> {
        return Observable.concatArrayEager(
            getItemsFromDb(),
            getItemsFromApi(header)
                .materialize()
                .map {
                    it.error?.let { view.showLoadingItemsError(": are you offline?") }
                    it
                }
                .filter { !it.isOnError }
                .dematerialize { it }
                .debounce(400, TimeUnit.MILLISECONDS)
        )
    }

    private fun getItemsFromDb(): Observable<List<Item>> {
        return dao.getItems()
            .toObservable()
            .doOnNext { println("Dispatching ${it.size} items from DB...") }
    }

    private fun getItemsFromApi(header: String): Observable<List<Item>> {
        return api.getAllItems(header)
            .doOnNext {
                println("Dispatching ${it.size} items from API...")
                storeItemsInDb(it)
            }
    }

    @SuppressLint("CheckResult")
    private fun storeItemsInDb(items: List<Item>) {
        Observable.fromCallable { dao.insertAll(items) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                println("Inserted ${items.size} items from API to DB...")
            }
    }
}