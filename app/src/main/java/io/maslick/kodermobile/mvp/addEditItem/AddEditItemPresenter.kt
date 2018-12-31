package io.maslick.kodermobile.mvp.addEditItem

import io.maslick.kodermobile.di.Properties.LOAD_DATA
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.Item
import io.maslick.kodermobile.rest.Status
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.setProperty

class AddEditItemPresenter(private val selectedItem: Item,
                           private val barkoderApi: IBarkoderApi,
                           val storage: IOAuth2AccessTokenStorage,
                           override var loadData: Boolean) : AddEditItemContract.Presenter, KoinComponent {
    override lateinit var view: AddEditItemContract.View

    override fun start() {
        if (loadData && selectedItem.id != null) view.populateItem(selectedItem)
    }

    override fun saveItem(item: Item) {
        if (item.title.isNullOrBlank()) view.showTitleValidationError()
        else if (item.barcode.isNullOrBlank()) view.showBarcodeValidationError()
        else {
            view.startLoadingIndicator()
            if (selectedItem.id == null)
                barkoderApi.postItem(item, header())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        view.stopLoadingIndicator()
                        if (it.status == Status.OK) view.showItems()
                        else view.showSaveItemError()
                    }, {
                        view.stopLoadingIndicator()
                        view.showSaveItemError()
                    })
            else {
                item.id = selectedItem.id
                barkoderApi.editItem(item, header())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        view.stopLoadingIndicator()
                        if (it.status == Status.OK) view.showItems()
                        else view.showSaveItemError()
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