package io.maslick.kodermobile.mvp.listItems

import android.annotation.SuppressLint
import android.app.Activity
import io.maslick.kodermobile.di.IBarkoderApi
import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.di.Status
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ItemsPresenter(val barkoderApi: IBarkoderApi) : ItemsContract.Presenter {

    override lateinit var view: ItemsContract.View

    var dataFetched = false

    override fun start() {
        if (!dataFetched) loadItems()
    }

    @SuppressLint("CheckResult")
    override fun loadItems() {
        view.setLoadingIndicator(true)
        barkoderApi.getAllItems()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                view.setLoadingIndicator(false)
                dataFetched = !dataFetched
                if (items.isEmpty()) view.showNoItems()
                else view.showItems(items)
            }, {
                view.showLoadingItemsError()
                view.setLoadingIndicator(false)
            })
    }

    override fun result(requestCode: Int, resultCode: Int) {
        if (AddEditItemActivity.REQUEST_ADD_ITEM == requestCode && Activity.RESULT_OK == resultCode)
            view.showSuccessfullySavedItem()
    }

    override fun addNewItem() {
        view.showAddItem()
    }

    override fun openItemDetail(item: Item) {
        view.showItemDetailUi(item)
    }

    @SuppressLint("CheckResult")
    override fun removeItem(item: Item) {
        barkoderApi.deleteItemWithId(item.id!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status == Status.OK) {
                    view.showDeleteOk("Item ${item.id} deleted")
                    loadItems()
                } else view.showErrorDeletingItem()
            }, { view.showErrorDeletingItem() } )
    }
}