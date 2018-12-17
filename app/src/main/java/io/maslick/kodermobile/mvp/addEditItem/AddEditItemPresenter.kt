package io.maslick.kodermobile.mvp.addEditItem

import io.maslick.kodermobile.di.IBarkoderApi
import io.maslick.kodermobile.di.Item
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AddEditItemPresenter(private val selectedItem: Item, private val barkoderApi: IBarkoderApi) : AddEditItemContract.Presenter {
    override lateinit var view: AddEditItemContract.View

    override fun start() {
        selectedItem.id?.apply { view.populateItem(selectedItem) }
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
}