package io.maslick.kodermobile.items

import io.maslick.kodermobile.di.IBarkoderApi
import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.di.Response
import io.maslick.kodermobile.di.Status
import io.maslick.kodermobile.helpers.RxSchedulersOverrideRule
import io.maslick.kodermobile.helpers.argumentCaptor
import io.maslick.kodermobile.helpers.capture
import io.maslick.kodermobile.helpers.kogda
import io.maslick.kodermobile.mvp.listItems.ItemsContract
import io.maslick.kodermobile.mvp.listItems.ItemsPresenter
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ItemsPresenterTest {

    @Rule @JvmField val rx = RxSchedulersOverrideRule()

    @Mock private lateinit var barkoderApi: IBarkoderApi
    @Mock private lateinit var itemsView: ItemsContract.View
    private lateinit var itemsPresenter: ItemsPresenter
    private lateinit var items: MutableList<Item>

    @Before
    fun beforeItemsPresenter() {
        MockitoAnnotations.initMocks(this)

        itemsPresenter = ItemsPresenter(barkoderApi)
        itemsPresenter.view = itemsView

        kogda(itemsView.isActive).thenReturn(true)

        items = mutableListOf(
            Item(1, "title1", "category1", "description1", "123456789", 1),
            Item(2, "title2", "category2", "description2", "123000000", 1),
            Item(3, "title3", "category3", "description3", "456000000", 1)
        )
    }

    @Test
    fun loadAllItemsIntoView() {
        kogda(barkoderApi.getAllItems()).thenReturn(Observable.just(items))
        itemsPresenter.loadItems()

        val inOrder = Mockito.inOrder(itemsView)
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)

        val argumentCaptor = argumentCaptor<List<Item>>()
        verify(itemsView).showItems(capture(argumentCaptor))
        Assert.assertEquals(3, argumentCaptor.value.size)
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
        kogda(barkoderApi.deleteItemWithId(items[0].id!!))
            .thenReturn(Observable.just(Response(Status.OK, null)))

        itemsPresenter.removeItem(items[0])
        verify(barkoderApi).deleteItemWithId(items[0].id!!)
        verify(itemsView).showDeleteOk(Mockito.anyString())
    }

    @Test
    fun clickDeleteItemError() {
        kogda(barkoderApi.deleteItemWithId(items[1].id!!))
            .thenReturn(Observable.just(Response(Status.ERROR, "Error deleting item!")))

        itemsPresenter.removeItem(items[1])
        verify(barkoderApi).deleteItemWithId(items[1].id!!)
        verify(itemsView).showErrorDeletingItem()
    }

    @Test
    fun noNetwork() {
        val inOrder = Mockito.inOrder(itemsView)
        kogda(barkoderApi.getAllItems()).thenReturn(Observable.error(UnknownHostException()))

        itemsPresenter.loadItems()
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)
        verify(itemsView).showLoadingItemsError()
    }

    @Test
    fun backendUnreachable() {
        val inOrder = Mockito.inOrder(itemsView)
        kogda(barkoderApi.getAllItems()).thenReturn(Observable.error(SocketTimeoutException()))

        itemsPresenter.loadItems()
        inOrder.verify(itemsView).setLoadingIndicator(true)
        inOrder.verify(itemsView).setLoadingIndicator(false)
        verify(itemsView).showLoadingItemsError()

    }
}