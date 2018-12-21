package io.maslick.kodermobile.addEditItem

import io.maslick.kodermobile.helpers.RxSchedulersOverrideRule
import io.maslick.kodermobile.helpers.kogda
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemContract
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemPresenter
import io.maslick.kodermobile.rest.IBarkoderApi
import io.maslick.kodermobile.rest.Item
import io.maslick.kodermobile.rest.Response
import io.maslick.kodermobile.rest.Status.ERROR
import io.maslick.kodermobile.rest.Status.OK
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class AddItemPresenterTest {

    @Rule @JvmField val rx = RxSchedulersOverrideRule()

    @Mock private lateinit var barkoderApi: IBarkoderApi
    @Mock private lateinit var addItemView: AddEditItemContract.View

    private lateinit var addItemPresenter: AddEditItemPresenter

    private val items = mutableListOf(
        Item(1, "title1", "category1", "description1", "123456789", 1),
        Item(2, "title2", "category2", "description2", "123000000", 1),
        Item(3, "title3", "category3", "description3", "456000000", 1)
    )

    @Before
    fun beforeItemsPresenter() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun addNewItemOk() {
        addItemPresenter = AddEditItemPresenter(Item(), barkoderApi, true)
        addItemPresenter.view = addItemView
        kogda(barkoderApi.postItem(items[0])).thenReturn(Observable.just(Response(OK, null)))

        addItemPresenter.saveItem(items[0])
        verify(barkoderApi).postItem(items[0])
        verify(addItemView).showItems()
    }

    @Test
    fun addNewItemError() {
        addItemPresenter = AddEditItemPresenter(Item(), barkoderApi, true)
        addItemPresenter.view = addItemView
        kogda(barkoderApi.postItem(items[1])).thenReturn(Observable.just(Response(ERROR, "This item already exists")))

        addItemPresenter.saveItem(items[1])
        verify(barkoderApi).postItem(items[1])
        verify(addItemView).showSaveItemError()
    }

    @Test
    fun showExistingItemAndEditIt() {
        addItemPresenter = AddEditItemPresenter(items[2], barkoderApi, true)
        addItemPresenter.view = addItemView
        kogda(barkoderApi.editItem(items[2])).thenReturn(Observable.just(Response(OK, null)))

        addItemPresenter.start()
        verify(addItemView).populateItem(items[2])

        addItemPresenter.saveItem(items[2])
        verify(barkoderApi).editItem(items[2])
        verify(addItemView).showItems()
    }

    @Test
    fun showExistingItemAndEditItError() {
        addItemPresenter = AddEditItemPresenter(items[0], barkoderApi, true)
        addItemPresenter.view = addItemView
        kogda(barkoderApi.editItem(items[0])).thenReturn(Observable.just(Response(ERROR, "Item with this barcode already exists!")))

        addItemPresenter.start()
        verify(addItemView).populateItem(items[0])

        addItemPresenter.saveItem(items[0])
        verify(barkoderApi).editItem(items[0])
        verify(addItemView).showSaveItemError()
    }

    @Test
    fun testBarcodeScanner() {
        addItemPresenter = AddEditItemPresenter(Item(), barkoderApi, true)
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
        addItemPresenter = AddEditItemPresenter(Item(), barkoderApi, true)
        addItemPresenter.view = addItemView

        addItemPresenter.saveItem(Item())
        verify(addItemView).showTitleValidationError()

        addItemPresenter.saveItem(Item(title = "title"))
        verify(addItemView).showBarcodeValidationError()
    }

    @Test
    fun testItemValidationBeforeEdit() {
        addItemPresenter = AddEditItemPresenter(items[2], barkoderApi, true)
        addItemPresenter.view = addItemView

        addItemPresenter.saveItem(Item())
        verify(addItemView).showTitleValidationError()

        addItemPresenter.saveItem(Item(title = "title"))
        verify(addItemView).showBarcodeValidationError()
    }
}