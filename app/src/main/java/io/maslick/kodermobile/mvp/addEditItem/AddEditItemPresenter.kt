package io.maslick.kodermobile.mvp.addEditItem

import io.maslick.kodermobile.di.IBarkoderApi
import io.maslick.kodermobile.di.Item
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AddEditItemPresenter(private val itemId: Int, val barkoderApi: IBarkoderApi) : AddEditItemContract.Presenter {
    override lateinit var view: AddEditItemContract.View

    override fun start() {}

    override fun saveItem(item: Item) {
        if (item.barcode.isNullOrEmpty()) view.showBarcodeValidationError()
        else if (item.quantity == null) view.showQuantityValidationError()
        else
            barkoderApi.postItem(item)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.showItems() }, { view.showSaveItemError() })
    }

    override fun scanCode() {
        view.initiateCodeScan()
    }

    override fun onNewBarcode(result: String?) {
        if (result == null) view.showScanCancelled()
        else view.showBarcode(result)
    }
}