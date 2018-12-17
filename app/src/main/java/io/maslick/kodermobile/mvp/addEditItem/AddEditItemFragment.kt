package io.maslick.kodermobile.mvp.addEditItem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.*
import android.widget.TextView
import com.google.zxing.integration.android.IntentIntegrator
import io.maslick.kodermobile.R
import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.helper.Helper.showSnackBar
import org.koin.android.ext.android.inject

class AddEditItemFragment : Fragment(), AddEditItemContract.View {
    override val presenter by inject<AddEditItemContract.Presenter>()

    override fun onResume() {
        super.onResume()
        presenter.view = this
        presenter.start()
    }

    private lateinit var title: TextView
    private lateinit var category: TextView
    private lateinit var description: TextView
    private lateinit var barcode: TextView
    private lateinit var quantity: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.additem_frag, container, false)

        with(root) {
            title = findViewById(R.id.editTitleFragment)
            category = findViewById(R.id.editCategoryFragment)
            description = findViewById(R.id.editDescriptionFragment)
            barcode = findViewById(R.id.editBarcodeFragment)
            quantity = findViewById(R.id.editQuantityFragment)

            activity!!.findViewById<FloatingActionButton>(R.id.fab_save_item).setOnClickListener {
                val titleStr = title.text.toString()
                val categoryStr = category.text.toString()
                val descStr = description.text.toString()
                val barcodeStr = barcode.text.toString()
                var num: Int? = null
                try { num = quantity.text.toString().toInt() } catch (e: Exception){}
                presenter.saveItem(Item(null, titleStr, categoryStr, descStr, barcodeStr, num))
            }
        }

        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.options_scan, menu)
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
        barcode.text = code
    }

    override fun populateItem(item: Item) {
        title.text = item.title
        barcode.text = item.barcode
        quantity.text = item.quantity.toString()
        category.text = item.category
        description.text = item.description
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

    override fun showQuantityValidationError() {
        view?.showSnackBar("Quantity cannot be empty", Snackbar.LENGTH_LONG)
    }

    override fun showSaveItemError() {
        view?.showSnackBar("Error while saving item", Snackbar.LENGTH_LONG)
    }
}

