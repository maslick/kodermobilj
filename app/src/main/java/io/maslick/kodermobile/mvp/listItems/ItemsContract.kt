package io.maslick.kodermobile.mvp.listItems

import io.maslick.kodermobile.mvp.BasePresenter
import io.maslick.kodermobile.mvp.BaseView
import io.maslick.kodermobile.rest.Item

interface ItemsContract {
    interface View : BaseView<Presenter> {
        var isActive: Boolean
        fun setLoadingIndicator(active: Boolean)
        fun showItems(items: List<Item>)
        fun showAddItem()
        fun showSuccessfullySavedItem()
        fun showLoadingItemsError()
        fun showNoItems()
        fun showDeleteOk(message: String)
        fun showErrorDeletingItem()
        fun showItemDetailUi(item: Item)
        fun logoutOk()
        fun logoutError(message: String? = null)
        fun startAuthActivity()
        fun showAuthOk(user: String)
        fun showAuthError()
    }

    interface Presenter : BasePresenter<View> {
        fun result(requestCode: Int, resultCode: Int)
        fun loadItems()
        fun addNewItem()
        fun openItemDetail(item: Item)
        fun removeItem(item: Item)
        fun logout()
    }
}