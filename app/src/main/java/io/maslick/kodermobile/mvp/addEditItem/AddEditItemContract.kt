package io.maslick.kodermobile.mvp.addEditItem

import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.mvp.BasePresenter
import io.maslick.kodermobile.mvp.BaseView

interface AddEditItemContract {
    interface View : BaseView<Presenter> {
        fun showItems()
        fun showBarcodeValidationError()
        fun showQuantityValidationError()
        fun showSaveItemError()
        fun initiateCodeScan()
        fun showScanCancelled()
        fun showBarcode(code: String)
    }
    interface Presenter : BasePresenter<View> {
        fun saveItem(item: Item)
        fun scanCode()
        fun onNewBarcode(result: String?)
    }
}