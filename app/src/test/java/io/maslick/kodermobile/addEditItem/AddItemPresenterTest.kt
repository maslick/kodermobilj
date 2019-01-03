package io.maslick.kodermobile.addEditItem

import io.maslick.kodermobile.helpers.RxImmediateSchedulerRule
import io.maslick.kodermobile.helpers.any
import io.maslick.kodermobile.helpers.kogda
import io.maslick.kodermobile.model.Item
import io.maslick.kodermobile.model.ItemDao
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemContract
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemPresenter
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.Resp
import io.maslick.kodermobile.rest.Status.ERROR
import io.maslick.kodermobile.rest.Status.OK
import io.reactivex.Maybe
import io.reactivex.Observable
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import retrofit2.Response

class AddItemPresenterTest {

    @Rule @JvmField val rule = MockitoJUnit.rule()!!
    @Rule @JvmField val testSchedulerRule = RxImmediateSchedulerRule()

    @Mock private lateinit var barkoderApi: IBarkoderApi
    @Mock private lateinit var itemDao: ItemDao
    @Mock private lateinit var addItemView: AddEditItemContract.View
    @Mock private lateinit var storage: IOAuth2AccessTokenStorage

    private lateinit var addItemPresenter: AddEditItemPresenter

    private val items = mutableListOf(
        Item(1, "title1", "category1", "description1", "123456789", 1),
        Item(2, "title2", "category2", "description2", "123000000", 1),
        Item(3, "title3", "category3", "description3", "456000000", 1)
    )

    @Before
    fun beforeItemsPresenter() {}

    @Test
    fun addNewItemOk() {
        addItemPresenter = AddEditItemPresenter("", barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView
        kogda(barkoderApi.postItem(any(), anyString())).thenReturn(Observable.just(Response.success(Resp(OK, null))))

        addItemPresenter.saveItem(items[0])
        verify(barkoderApi).postItem(any(), anyString())
        verify(addItemView).showItems()
    }

    @Test
    fun addNewItemError() {
        addItemPresenter = AddEditItemPresenter("", barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView
        kogda(barkoderApi.postItem(any(), anyString())).thenReturn(Observable.just(Response.success(Resp(ERROR, "This item already exists"))))

        addItemPresenter.saveItem(items[1])
        verify(barkoderApi).postItem(any(), anyString())
        verify(addItemView).showSaveItemError()
    }

    @Test
    fun addNewItemNetworkError() {
        addItemPresenter = AddEditItemPresenter("", barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView

        kogda(barkoderApi.postItem(any(), anyString())).thenReturn(Observable.just(Response.error(401, ResponseBody.create(null, ""))))
        addItemPresenter.saveItem(items[1])
        verify(barkoderApi, atLeastOnce()).postItem(any(), anyString())
        verify(addItemView).showHttpError("Error while saving item: are you logged in?")
        verify(addItemView, never()).showSaveItemError()
        verify(addItemView, never()).showItems()

        kogda(barkoderApi.postItem(any(), anyString())).thenReturn(Observable.just(Response.error(403, ResponseBody.create(null, ""))))
        addItemPresenter.saveItem(items[1])
        verify(barkoderApi, atLeastOnce()).postItem(any(), anyString())
        verify(addItemView).showHttpError("Error while saving item: access forbidden")
        verify(addItemView, never()).showSaveItemError()
        verify(addItemView, never()).showItems()

        kogda(barkoderApi.postItem(any(), anyString())).thenReturn(Observable.just(Response.error(503, ResponseBody.create(null, ""))))
        addItemPresenter.saveItem(items[1])
        verify(barkoderApi, atLeastOnce()).postItem(any(), anyString())
        verify(addItemView).showHttpError("Error while saving item: service unavailable")
        verify(addItemView, never()).showSaveItemError()
        verify(addItemView, never()).showItems()
    }

    @Test
    fun showExistingItemAndEditIt() {
        addItemPresenter = AddEditItemPresenter(items[2].id.toString(), barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView
        kogda(itemDao.getItemById(anyInt())).thenReturn(Maybe.just(items[2]))
        kogda(barkoderApi.getItemWithId(anyInt(), anyString())).thenReturn(Observable.just(Response.success(items[2])))
        kogda(barkoderApi.editItem(any(), anyString())).thenReturn(Observable.just(Response.success(Resp(OK, null))))

        addItemPresenter.start()
        verify(addItemView, atLeastOnce()).populateItem(items[2])

        addItemPresenter.saveItem(items[2])
        verify(barkoderApi).editItem(any(), anyString())
        verify(addItemView).showItems()
    }

    @Test
    fun showExistingItemAndEditItError() {
        addItemPresenter = AddEditItemPresenter(items[0].id.toString(), barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView
        kogda(itemDao.getItemById(anyInt())).thenReturn(Maybe.just(items[0]))
        kogda(barkoderApi.getItemWithId(anyInt(), anyString())).thenReturn(Observable.just(Response.success(items[0])))
        kogda(barkoderApi.editItem(any(), anyString()))
            .thenReturn(Observable.just(Response.success(Resp(ERROR, "Item with this barcode already exists!"))))

        addItemPresenter.start()
        verify(addItemView).populateItem(items[0])

        addItemPresenter.saveItem(items[0])
        verify(barkoderApi).editItem(any(), anyString())
        verify(addItemView).showSaveItemError()
    }

    @Test
    fun editItemNetworkError() {
        addItemPresenter = AddEditItemPresenter(items[0].id.toString(), barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView

        kogda(barkoderApi.editItem(any(), anyString())).thenReturn(Observable.just(Response.error(401, ResponseBody.create(null, ""))))
        addItemPresenter.saveItem(items[1])
        verify(barkoderApi, atLeastOnce()).editItem(any(), anyString())
        verify(addItemView).showHttpError("Error while saving item: are you logged in?")
        verify(addItemView, never()).showSaveItemError()
        verify(addItemView, never()).showItems()

        kogda(barkoderApi.editItem(any(), anyString())).thenReturn(Observable.just(Response.error(403, ResponseBody.create(null, ""))))
        addItemPresenter.saveItem(items[1])
        verify(barkoderApi, atLeastOnce()).editItem(any(), anyString())
        verify(addItemView).showHttpError("Error while saving item: access forbidden")
        verify(addItemView, never()).showSaveItemError()
        verify(addItemView, never()).showItems()

        kogda(barkoderApi.editItem(any(), anyString())).thenReturn(Observable.just(Response.error(503, ResponseBody.create(null, ""))))
        addItemPresenter.saveItem(items[1])
        verify(barkoderApi, atLeastOnce()).editItem(any(), anyString())
        verify(addItemView).showHttpError("Error while saving item: service unavailable")
        verify(addItemView, never()).showSaveItemError()
        verify(addItemView, never()).showItems()
    }

    @Test
    fun testBarcodeScanner() {
        addItemPresenter = AddEditItemPresenter("", barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView

        addItemPresenter.scanCode()
        verify(addItemView).initiateCodeScan()

        addItemPresenter.onNewBarcode("12345")
        verify(addItemView).showBarcode("12345")

        addItemPresenter.onNewBarcode(null)
        verify(addItemView).showScanCancelled()
    }

    @Test
    fun testItemValidationBeforeSave() {
        addItemPresenter = AddEditItemPresenter("", barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView

        addItemPresenter.saveItem(Item())
        verify(addItemView).showTitleValidationError()

        addItemPresenter.saveItem(Item(title = "title"))
        verify(addItemView).showBarcodeValidationError()
    }

    @Test
    fun testItemValidationBeforeEdit() {
        addItemPresenter = AddEditItemPresenter(items[2].id.toString(), barkoderApi, itemDao, storage, true)
        addItemPresenter.view = addItemView

        addItemPresenter.saveItem(Item())
        verify(addItemView).showTitleValidationError()

        addItemPresenter.saveItem(Item(title = "title"))
        verify(addItemView).showBarcodeValidationError()
    }
}