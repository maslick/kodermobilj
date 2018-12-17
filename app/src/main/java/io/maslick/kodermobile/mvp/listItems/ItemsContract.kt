package io.maslick.kodermobile.mvp.listItems

import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.mvp.BasePresenter
import io.maslick.kodermobile.mvp.BaseView

interface ItemsContract {
    interface View : BaseView<Presenter> {
        var isActive: Boolean
        fun setLoadingIndicator(active: Boolean)
        fun showItems(items: List<Item>)
        fun showAddItem()
        fun showItemDetailUi(item: Item)
        fun showSuccessfullySavedItem()
        fun showLoadingItemsError()
        fun showNoItems()
        fun showItem(item: Item)
    }

    interface Presenter : BasePresenter<View> {
        fun stop()
        fun result(requestCode: Int, resultCode: Int)
        fun loadItems()
        fun addNewItem()
        fun openItemDetail(item: Item)
        fun editItem(item: Item)
        fun removeItem(item: Item)
    }
}