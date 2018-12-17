package io.maslick.kodermobile.mvp.addEditItem

import io.maslick.kodermobile.di.IBarkoderApi
import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.di.Properties.LOAD_DATA
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.setProperty

class AddEditItemPresenter(private val selectedItem: Item,
                           private val barkoderApi: IBarkoderApi,
                           override var loadData: Boolean) : AddEditItemContract.Presenter, KoinComponent {
    override lateinit var view: AddEditItemContract.View

    override fun start() {
        if (loadData && selectedItem.id != null) view.populateItem(selectedItem)
    }

    override fun saveItem(item: Item) {
        if (item.barcode.isNullOrEmpty()) view.showBarcodeValidationError()
        else if (item.quantity == null) view.showQuantityValidationError()
        else
            if (selectedItem.id == null)
                barkoderApi.postItem(item)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ view.showItems() }, { view.showSaveItemError() })
            else {
                item.id = selectedItem.id
                barkoderApi.editItem(item)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ view.showItems() }, { view.showSaveItemError() })
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
}