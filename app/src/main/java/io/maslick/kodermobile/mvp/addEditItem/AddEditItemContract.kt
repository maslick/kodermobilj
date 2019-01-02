package io.maslick.kodermobile.mvp.addEditItem

import io.maslick.kodermobile.mvp.BasePresenter
import io.maslick.kodermobile.mvp.BaseView
import io.maslick.kodermobile.model.Item

interface AddEditItemContract {
    interface View : BaseView<Presenter> {
        fun showItems()
        fun populateItem(item: Item)
        fun showBarcodeValidationError()
        fun showTitleValidationError()
        fun showSaveItemError()
        fun initiateCodeScan()
        fun showScanCancelled()
        fun showBarcode(code: String)
        fun startLoadingIndicator()
        fun stopLoadingIndicator()
    }
    interface Presenter : BasePresenter<View> {
        var loadData: Boolean
        fun saveItem(item: Item)
        fun scanCode()
        fun onNewBarcode(result: String?)
        fun stop()
    }
}