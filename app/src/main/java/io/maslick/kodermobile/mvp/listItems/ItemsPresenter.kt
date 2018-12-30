package io.maslick.kodermobile.mvp.listItems

import android.annotation.SuppressLint
import android.app.Activity
import io.maslick.kodermobile.Config
import io.maslick.kodermobile.helper.Helper
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemActivity
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.IKeycloakRest
import io.maslick.kodermobile.rest.Item
import io.maslick.kodermobile.rest.Status
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ItemsPresenter(private val barkoderApi: IBarkoderApi,
                     private val keycloakApi: IKeycloakRest,
                     private val storage: IOAuth2AccessTokenStorage) : ItemsContract.Presenter {

    override lateinit var view: ItemsContract.View

    var dataFetched = false

    override fun start() {
        if (Helper.isTokenExpired(storage.getStoredAccessToken())) return
        if (!dataFetched) loadItems()
    }

    @SuppressLint("CheckResult")
    override fun loadItems() {
        view.setLoadingIndicator(true)
        barkoderApi.getAllItems(header())
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
        barkoderApi.deleteItemWithId(item.id!!, header())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.status == Status.OK) {
                    view.showDeleteOk("Item ${item.id} deleted")
                    loadItems()
                } else view.showErrorDeletingItem()
            }, { view.showErrorDeletingItem() } )
    }

    @SuppressLint("CheckResult")
    override fun logout() {
        storage.getStoredAccessToken()?.refreshToken?.apply {
            keycloakApi.logout(Config.clientId, this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    storage.removeAccessToken()
                    view.logoutOk()
                }, {
                    it.printStackTrace()
                    view.logoutError(it.message)
                })
        }
    }

    private fun header() = "Bearer ${storage.getStoredAccessToken()?.accessToken}"
}