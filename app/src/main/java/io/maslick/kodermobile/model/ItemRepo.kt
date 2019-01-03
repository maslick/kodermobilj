package io.maslick.kodermobile.model

import android.annotation.SuppressLint
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemContract
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.rest.IBarkoderApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class ItemRepo(private val api: IBarkoderApi, private val dao: ItemDao) {

    fun getAllItems(header: String, view: ItemsContract.View): Observable<List<Item>> {
        return Observable.concatArrayEager(
            getItemsFromDb(),
            getItemsFromApi(header, view)
        )
    }

    fun getItem(id: Int, header: String, view: AddEditItemContract.View): Observable<Item> {
        return Observable.concatEager(
            listOf(
                getItemFromDb(id),
                getItemFromApi(id, header, view)
            )
        )
    }

    private fun getItemsFromDb(): Observable<List<Item>> {
        return dao.getItems()
            .toObservable()
            .doOnNext { println("Dispatching ${it.size} items from DB...") }
    }

    private fun getItemFromDb(id: Int): Observable<Item> {
        return dao.getItemById(id)
            .toObservable()
            .doOnNext { println("Dispatching item #${it.id} from DB...") }
    }

    @SuppressLint("CheckResult")
    private fun getItemsFromApi(header: String, view: ItemsContract.View): Observable<List<Item>> {
        return api.getAllItems(header)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                when(it.code()) {
                    401 -> view.showLoadingItemsError("Error while loading items: are you logged in?")
                    403 -> view.showLoadingItemsError("Error while loading items: access forbidden")
                    503 -> view.showLoadingItemsError("Error while loading items: service unavailable")
                }
            }
            .materialize()
            .map {
                it.error?.let { t ->
                    when {
                        UnknownHostException::class.isInstance(t) -> view.showLoadingItemsError("Error while loading items: are you offline?")
                        SocketTimeoutException::class.isInstance(t) -> view.showLoadingItemsError("Error while loading items: timeout, try again later")
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

    private fun getItemFromApi(id: Int, header: String, view: AddEditItemContract.View): Observable<Item> {
        return api.getItemWithId(id, header)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                when(it.code()) {
                    401 -> view.showHttpError("Error loading item: are you logged in?")
                    403 -> view.showHttpError("Error loading item: access forbidden")
                    503 -> view.showHttpError("Error loading item: service unavailable")
                }
            }
            .materialize()
            .map {
                it.error?.let { t ->
                    when {
                        UnknownHostException::class.isInstance(t) -> view.showHttpError("Error loading item: are you offline?")
                        SocketTimeoutException::class.isInstance(t) -> view.showHttpError("Error loading item: timeout, try again later")
                    }
                }
                it
            }
            .filter { !it.isOnError }
            .dematerialize { it }
            .map { it.body() ?: Item() }
            .doOnNext { println("Dispatching item #${it.id} from DB...") }
    }

    @SuppressLint("CheckResult")
    private fun storeItemsInDb(items: List<Item>) {
        Observable.fromCallable {
            dao.deleteAll()
            dao.insertAll(items)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                println("Inserted ${items.size} items from API to DB...")
            }
    }

    @SuppressLint("CheckResult")
    private fun storeItemInDb(item: Item) {
        Observable.fromCallable { dao.insert(item) }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe {
                println("Inserted item #${item.id} from API to DB...")
            }
    }
}