package io.maslick.kodermobile.mvp.listItems

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import io.maslick.kodermobile.Config
import io.maslick.kodermobile.helper.Helper
import io.maslick.kodermobile.model.Item
import io.maslick.kodermobile.model.ItemDao
import io.maslick.kodermobile.model.ItemRepo
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemActivity.Companion.ADD_ITEM_REQUEST_CODE
import io.maslick.kodermobile.mvp.listItems.ItemsActivity.Companion.AUTHORIZATION_REQUEST_CODE
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.oauth.parseJwtToken
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.IKeycloakRest
import io.maslick.kodermobile.rest.Resp
import io.maslick.kodermobile.rest.Status
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ItemsPresenter(private val barkoderApi: IBarkoderApi,
                     private val keycloakApi: IKeycloakRest,
                     private val dao: ItemDao,
                     private val storage: IOAuth2AccessTokenStorage) : ItemsContract.Presenter {

    override lateinit var view: ItemsContract.View
    private var repo: ItemRepo = ItemRepo(barkoderApi, dao)

    var dataFetched = false

    override fun start() {
        if (Helper.isRefreshTokenExpired(storage.getStoredAccessToken())) {
            view.startAuthActivity()
            return
        }
        if (!dataFetched) loadItems()
    }

    @SuppressLint("CheckResult")
    override fun loadItems() {
        view.setLoadingIndicator(true)
        repo.getAllItems(header(), view)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                view.setLoadingIndicator(false)
                dataFetched = !dataFetched
                if (items.isNotEmpty()) view.showItems(items)
            }, {
                it.printStackTrace()
                view.showLoadingItemsError()
                view.setLoadingIndicator(false)
            })
    }

    override fun result(requestCode: Int, resultCode: Int) {
        when (requestCode) {
            ADD_ITEM_REQUEST_CODE -> {
                if (resultCode == RESULT_OK)
                    view.showSuccessfullySavedItem()
            }
            AUTHORIZATION_REQUEST_CODE -> {
                val user = parseJwtToken(storage.getStoredAccessToken()?.accessToken)
                if (resultCode == RESULT_OK) view.showAuthOk(user.email ?: "n/a")
                else view.showAuthError()
            }
        }
    }

    override fun addNewItem() {
        view.showAddItem()
    }

    override fun openItemDetail(item: Item) {
        view.showItemDetailUi(item.id!!)
    }

    @SuppressLint("CheckResult")
    override fun removeItem(item: Item) {
        barkoderApi.deleteItemWithId(item.id!!, header())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                when(it.code()) {
                    401 -> view.showError("Error removing item: are you logged in?")
                    403 -> view.showError("Error removing item: access forbidden")
                    503 -> view.showError("Error removing item: service unavailable")
                }
            }
            .materialize()
            .map {
                it.error?.let { t ->
                    when {
                        UnknownHostException::class.isInstance(t) -> view.showError("Error removing item: are you offline?")
                        SocketTimeoutException::class.isInstance(t) -> view.showError("Error removing item: timeout, try again later")
                    }
                }
                it
            }
            .filter { !it.isOnError }
            .dematerialize { it }
            .map { it.body() ?: Resp(Status.NETWORK_ERROR, "connection issue :(") }
            .subscribe({
                if (it.status == Status.OK) {
                    view.showDeleteOk("Item ${item.id} deleted")
                    loadItems()
                }
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