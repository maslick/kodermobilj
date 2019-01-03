package io.maslick.kodermobile.mvp.addEditItem

import android.annotation.SuppressLint
import io.maslick.kodermobile.di.Properties.LOAD_DATA
import io.maslick.kodermobile.model.Item
import io.maslick.kodermobile.model.ItemDao
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.Resp
import io.maslick.kodermobile.rest.Status
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.setProperty
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AddEditItemPresenter(private val selectedItem: String,
                           private val barkoderApi: IBarkoderApi,
                           private val room: ItemDao,
                           private val storage: IOAuth2AccessTokenStorage,
                           override var loadData: Boolean) : AddEditItemContract.Presenter, KoinComponent {

    override lateinit var view: AddEditItemContract.View

    @SuppressLint("CheckResult")
    override fun start() {
        if (loadData && selectedItem.isNotEmpty()) {
            room.getItemByBarcode(selectedItem)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ item ->
                    item?.let {
                        view.populateItem(it)
                    } ?: view.showItems()
                }, {
                    it.printStackTrace()
                    view.showItems()
                })
        }
    }

    override fun saveItem(item: Item) {
        if (item.title.isNullOrBlank()) view.showTitleValidationError()
        else if (item.barcode.isNullOrBlank()) view.showBarcodeValidationError()
        else {
            view.startLoadingIndicator()
            if (selectedItem.isEmpty())
                barkoderApi.postItem(item, header())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        when(it.code()) {
                            401 -> view.showHttpError("Error while saving item: are you logged in?")
                            403 -> view.showHttpError("Error while saving item: access forbidden")
                            503 -> view.showHttpError("Error while saving item: service unavailable")
                        }
                    }
                    .materialize()
                    .map {
                        it.error?.let { t ->
                            when {
                                UnknownHostException::class.isInstance(t) -> view.showHttpError("Error while saving item: are you offline?")
                                SocketTimeoutException::class.isInstance(t) -> view.showHttpError("Error while saving item: timeout, try again later")
                            }
                            view.stopLoadingIndicator()
                        }
                        it
                    }
                    .filter { !it.isOnError }
                    .dematerialize { it }
                    .map { it.body() ?: Resp(Status.NETWORK_ERROR, "connection issue :(") }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        view.stopLoadingIndicator()
                        if (it.status == Status.OK) view.showItems()
                        if (it.status == Status.ERROR) view.showSaveItemError()
                    }, {
                        view.stopLoadingIndicator()
                        view.showSaveItemError()
                    })
            else {
                barkoderApi.editItem(item, header())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        when(it.code()) {
                            401 -> view.showHttpError("Error while saving item: are you logged in?")
                            403 -> view.showHttpError("Error while saving item: access forbidden")
                            503 -> view.showHttpError("Error while saving item: service unavailable")
                        }
                    }
                    .materialize()
                    .map {
                        it.error?.let { t ->
                            when {
                                UnknownHostException::class.isInstance(t) -> view.showHttpError("Error while saving item: are you offline?")
                                SocketTimeoutException::class.isInstance(t) -> view.showHttpError("Error while saving item: timeout, try again later")
                            }
                            view.stopLoadingIndicator()
                        }
                        it
                    }
                    .filter { !it.isOnError }
                    .dematerialize { it }
                    .map { it.body() ?: Resp(Status.NETWORK_ERROR, "connection issue :(") }
                    .subscribe({
                        view.stopLoadingIndicator()
                        if (it.status == Status.OK) view.showItems()
                        if (it.status == Status.ERROR) view.showSaveItemError()
                    }, {
                        view.stopLoadingIndicator()
                        view.showSaveItemError()
                    })
            }
        }
    }

    override fun scanCode() {
        view.initiateCodeScan()
    }

    override fun onNewBarcode(result: String?) {
        if (result == null) view.showScanCancelled()
        else view.showBarcode(result)
    }

    override fun stop() {
        setProperty(LOAD_DATA, loadData)
    }

    private fun header() = "Bearer ${storage.getStoredAccessToken()?.accessToken}"
}