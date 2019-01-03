package io.maslick.kodermobile.items

import io.maslick.kodermobile.helpers.RxImmediateSchedulerRule
import io.maslick.kodermobile.helpers.argumentCaptor
import io.maslick.kodermobile.helpers.capture
import io.maslick.kodermobile.helpers.kogda
import io.maslick.kodermobile.model.Item
import io.maslick.kodermobile.model.ItemDao
import io.maslick.kodermobile.model.ItemRepo
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.mvp.listItems.ItemsPresenter
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.IKeycloakRest
import io.maslick.kodermobile.rest.KeycloakToken
import io.maslick.kodermobile.rest.Resp
import io.maslick.kodermobile.rest.Status.ERROR
import io.maslick.kodermobile.rest.Status.OK
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import io.maslick.kodermobile.helpers.any as myAny

class ItemsPresenterTest {

    @Rule @JvmField val rule = MockitoJUnit.rule()!!
    @Rule @JvmField val testSchedulerRule = RxImmediateSchedulerRule()

    @Mock private lateinit var barkoderApi: IBarkoderApi
    @Mock private lateinit var itemsView: ItemsContract.View
    @Mock private lateinit var itemDao: ItemDao
    @Mock private lateinit var keycloakApi: IKeycloakRest
    @Mock private lateinit var storage: IOAuth2AccessTokenStorage
    private lateinit var itemsPresenter: ItemsPresenter
    private lateinit var items: MutableList<Item>

    @Before
    fun beforeItemsPresenter() {
        itemsPresenter = ItemsPresenter(barkoderApi, ItemRepo(barkoderApi, itemDao), keycloakApi, storage)
        itemsPresenter.view = itemsView

        items = mutableListOf(
            Item(1, "title1", "category1", "description1", "123456789", 1),
            Item(2, "title2", "category2", "description2", "123000000", 1),
            Item(3, "title3", "category3", "description3", "456000000", 1)
        )
    }

    @Test
    fun loadAllItemsIntoView() {
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.just(Response.success(items.toList())))
        kogda(itemDao.getItems()).thenReturn(Single.just(emptyList()))
        itemsPresenter.loadItems()
        val inOrder = inOrder(itemsView)
        inOrder.verify(itemsView, atLeastOnce()).setLoadingIndicator(true)
        inOrder.verify(itemsView, atLeastOnce()).setLoadingIndicator(false)

        val captor = argumentCaptor<List<Item>>()
        verify(itemsView).showItems(capture(captor))
        verify(itemDao).getItems()
        Assert.assertEquals(3, captor.value.size)
    }

    @Test
    fun dontLoadItemsIfTokenExpired() {
        kogda(storage.getStoredAccessToken()).thenReturn(KeycloakToken(tokenExpirationDate = null))
        itemsPresenter.start()
        verify(itemsView, never()).showItems(anyList())
    }

    @Test
    fun loadItemsIfTokenValid() {
        val expiryDate = GregorianCalendar(2099, 1, 1)
        val token = KeycloakToken(tokenExpirationDate = expiryDate, refreshTokenExpirationDate = expiryDate)
        kogda(storage.getStoredAccessToken()).thenReturn(token)
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.just(Response.success(emptyList())))
        kogda(itemDao.getItems()).thenReturn(Single.just(items.toList()))
        itemsPresenter.start()
        verify(itemsView).showItems(anyList())
    }

    @Test
    fun clickOnFab_showsAddItemUI() {
        itemsPresenter.addNewItem()
        verify(itemsView).showAddItem()
    }

    @Test
    fun clickOnItemDetail() {
        val testItem = Item()
        itemsPresenter.openItemDetail(testItem)
        verify(itemsView).showItemDetailUi(testItem)
    }

    @Test
    fun clickDeleteItem() {
        kogda(barkoderApi.deleteItemWithId(anyInt(), anyString()))
            .thenReturn(Observable.just(Resp(OK, null)))

        itemsPresenter.removeItem(items[0])
        verify(barkoderApi).deleteItemWithId(anyInt(), anyString())
        verify(itemsView).showDeleteOk(anyString())
    }

    @Test
    fun clickDeleteItemError() {
        kogda(barkoderApi.deleteItemWithId(anyInt(), anyString()))
            .thenReturn(Observable.just(Resp(ERROR, "Error deleting item!")))

        itemsPresenter.removeItem(items[1])
        verify(barkoderApi).deleteItemWithId(anyInt(), anyString())
        verify(itemsView).showErrorDeletingItem()
    }

    @Test
    fun noNetwork() {
        val inOrder = inOrder(itemsView)
        kogda(itemDao.getItems()).thenReturn(Single.just((items + Item()).toList()))
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.error(UnknownHostException()))

        itemsPresenter.loadItems()
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)
        verify(itemsView).showLoadingItemsError(": are you offline?")

        val captor = argumentCaptor<List<Item>>()
        verify(itemsView).showItems(capture(captor))
        Assert.assertEquals(4, captor.value.size)
    }

    @Test
    fun backendUnreachable() {
        val inOrder = inOrder(itemsView)
        kogda(itemDao.getItems()).thenReturn(Single.just((items).toList()))
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.error(SocketTimeoutException()))

        itemsPresenter.loadItems()
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)
        verify(itemsView).showLoadingItemsError(": timeout, try again later")
    }

    @Test
    fun error401() {
        kogda(itemDao.getItems()).thenReturn(Single.just(emptyList()))
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.just(Response.error(401, ResponseBody.create(null, ""))))

        itemsPresenter.loadItems()
        verify(itemsView).showLoadingItemsError(": are you logged in?")
        verify(itemsView, never()).showItems(anyList())
    }

    @Test
    fun error403() {
        kogda(itemDao.getItems()).thenReturn(Single.just(emptyList()))
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.just(Response.error(403, ResponseBody.create(null, ""))))

        itemsPresenter.loadItems()
        verify(itemsView).showLoadingItemsError(": access forbidden")
        verify(itemsView, never()).showItems(anyList())
    }

    @Test
    fun error503() {
        kogda(itemDao.getItems()).thenReturn(Single.just(listOf(items[0])))
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.just(Response.error(503, ResponseBody.create(null, ""))))

        itemsPresenter.loadItems()
        verify(itemsView).showLoadingItemsError(": service unavailable")
        verify(itemsView, atLeastOnce()).showItems(anyList())
    }
}