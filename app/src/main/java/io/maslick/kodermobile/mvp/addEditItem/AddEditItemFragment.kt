package io.maslick.kodermobile.mvp.addEditItem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.*
import android.widget.EditText
import android.widget.NumberPicker
import com.google.zxing.integration.android.IntentIntegrator
import io.maslick.kodermobile.R
import io.maslick.kodermobile.helper.AndroidUtils
import io.maslick.kodermobile.helper.Helper.showSnackBar
import io.maslick.kodermobile.rest.Item
import org.koin.android.ext.android.inject

class AddEditItemFragment : Fragment(), AddEditItemContract.View {
    override val presenter by inject<AddEditItemContract.Presenter>()

    override fun onResume() {
        super.onResume()
        presenter.view = this
        presenter.start()
    }

    private lateinit var title: EditText
    private lateinit var category: EditText
    private lateinit var description: EditText
    private lateinit var barcode: EditText
    private lateinit var quantity: NumberPicker
    private lateinit var progressOverlay: View
    private lateinit var contents: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.additem_frag, container, false)

        with(root) {
            title = findViewById(R.id.editTitleFragment)
            category = findViewById(R.id.editCategoryFragment)
            description = findViewById(R.id.editDescriptionFragment)
            barcode = findViewById(R.id.editBarcodeFragment)
            quantity = findViewById(R.id.editQuantityFragment)
            progressOverlay = findViewById(R.id.progress_overlay)
            contents = findViewById(R.id.addEditItemContents)

            with(quantity) {
                minValue = 0
                maxValue = 99
                setFormatter { "$it pcs." }
                wrapSelectorWheel = true
            }

            activity!!.findViewById<FloatingActionButton>(R.id.fab_save_item).setOnClickListener {
                val titleStr = title.text.toString()
                val categoryStr = category.text.toString()
                val descStr = description.text.toString()
                val barcodeStr = barcode.text.toString()
                presenter.saveItem(Item(null, titleStr, categoryStr, descStr, barcodeStr, quantity.value))
            }
        }

        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.item_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) { R.id.scanBarcode -> presenter.scanCode() }
        return super.onOptionsItemSelected(item)
    }

    override fun initiateCodeScan() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("Scan your invoice, pa-lease :) \n")
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result ?: super.onActivityResult(requestCode, resultCode, data)

        presenter.onNewBarcode(result.contents)
    }

    override fun showScanCancelled() {
        view?.showSnackBar("Scan cancelled", Snackbar.LENGTH_LONG)
    }

    override fun showBarcode(code: String) {
        barcode.setText(code)
    }

    override fun populateItem(item: Item) {
        title.setText(item.title)
        barcode.setText(item.barcode)
        quantity.value = item.quantity ?: 0
        category.setText(item.category)
        description.setText(item.description)
        presenter.loadData = false
    }

    override fun showItems() {
        with(activity!!) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun showBarcodeValidationError() {
        view?.showSnackBar("Barcode cannot be empty", Snackbar.LENGTH_LONG)
    }

    override fun showTitleValidationError() {
        view?.showSnackBar("Title cannot be empty", Snackbar.LENGTH_LONG)
    }

    override fun showSaveItemError() {
        view?.showSnackBar("Error while saving item", Snackbar.LENGTH_LONG)
    }

    override fun startLoadingIndicator() {
        contents.alpha = 0.7f
        contents.setOnTouchListener { _, _ ->  false }
        AndroidUtils.animateView(progressOverlay, View.VISIBLE, 1f, 200)
    }

    override fun stopLoadingIndicator() {
        contents.alpha = 1.0f
        contents.setOnTouchListener { v, event -> v.onTouchEvent(event) }
        AndroidUtils.animateView(progressOverlay, View.GONE, 0f, 200)
    }
}

