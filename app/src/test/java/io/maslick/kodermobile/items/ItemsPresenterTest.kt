package io.maslick.kodermobile.items

import io.maslick.kodermobile.helpers.RxImmediateSchedulerRule
import io.maslick.kodermobile.helpers.argumentCaptor
import io.maslick.kodermobile.helpers.capture
import io.maslick.kodermobile.helpers.kogda
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.mvp.listItems.ItemsPresenter
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.IKeycloakRest
import io.maslick.kodermobile.rest.Item
import io.maslick.kodermobile.rest.Response
import io.maslick.kodermobile.rest.Status.ERROR
import io.maslick.kodermobile.rest.Status.OK
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ItemsPresenterTest {

    @Rule @JvmField val rule = MockitoJUnit.rule()!!
    @Rule @JvmField val testSchedulerRule = RxImmediateSchedulerRule()

    @Mock private lateinit var barkoderApi: IBarkoderApi
    @Mock private lateinit var itemsView: ItemsContract.View
    @Mock private lateinit var keycloakApi: IKeycloakRest
    @Mock private lateinit var storage: IOAuth2AccessTokenStorage
    private lateinit var itemsPresenter: ItemsPresenter
    private lateinit var items: MutableList<Item>

    @Before
    fun beforeItemsPresenter() {
        itemsPresenter = ItemsPresenter(barkoderApi, keycloakApi, storage)
        itemsPresenter.view = itemsView

        items = mutableListOf(
            Item(1, "title1", "category1", "description1", "123456789", 1),
            Item(2, "title2", "category2", "description2", "123000000", 1),
            Item(3, "title3", "category3", "description3", "456000000", 1)
        )
    }

    @Test
    fun loadAllItemsIntoView() {
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.just(items))
        itemsPresenter.loadItems()
        val inOrder = inOrder(itemsView)
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)

        val captor = argumentCaptor<List<Item>>()
        verify(itemsView).showItems(capture(captor))
        verify(barkoderApi).getAllItems(anyString())
        Assert.assertEquals(3, captor.value.size)
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
            .thenReturn(Observable.just(Response(OK, null)))

        itemsPresenter.removeItem(items[0])
        verify(barkoderApi).deleteItemWithId(anyInt(), anyString())
        verify(itemsView).showDeleteOk(anyString())
    }

    @Test
    fun clickDeleteItemError() {
        kogda(barkoderApi.deleteItemWithId(anyInt(), anyString()))
            .thenReturn(Observable.just(Response(ERROR, "Error deleting item!")))

        itemsPresenter.removeItem(items[1])
        verify(barkoderApi).deleteItemWithId(anyInt(), anyString())
        verify(itemsView).showErrorDeletingItem()
    }

    @Test
    fun noNetwork() {
        val inOrder = inOrder(itemsView)
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.error(UnknownHostException()))

        itemsPresenter.loadItems()
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)
        verify(itemsView).showLoadingItemsError()
    }

    @Test
    fun backendUnreachable() {
        val inOrder = inOrder(itemsView)
        kogda(barkoderApi.getAllItems(anyString())).thenReturn(Observable.error(SocketTimeoutException()))

        itemsPresenter.loadItems()
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)
        verify(itemsView).showLoadingItemsError()

    }
}