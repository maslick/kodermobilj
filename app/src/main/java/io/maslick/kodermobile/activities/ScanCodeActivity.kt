package io.maslick.kodermobile.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.maslick.kodermobile.R
import io.maslick.kodermobile.di.IBarkoderApi
import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.storage.IOAuth2AccessTokenStorage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_scan_code.*
import org.koin.android.ext.android.inject

class ScanCodeActivity : RxAppCompatActivity() {

    val barkoderApi: IBarkoderApi by inject()
    val storage by inject<IOAuth2AccessTokenStorage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_code)
        sendItemBtn.setOnClickListener(handlePostItem())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_scan, menu)
        return true
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.scanBarcode -> startScan()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startScan() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan your invoice, pa-lease :) \n")
        integrator.setBarcodeImageEnabled(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        result ?: super.onActivityResult(requestCode, resultCode, data)

        if (result.contents == null)
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
        else
            onNewBarcode(result.contents)
    }

    private fun onNewBarcode(code: String) {
        editBarcode.setText(code)
    }

    private fun handlePostItem(): (v: View) -> Unit {
        return {
            if (editTitle.text.isNotEmpty() && editBarcode.text.isNotEmpty() && editQuantity.text.isNotEmpty()) {
                editTitle.error = null
                editBarcode.error = null
                editQuantity.error = null
                val token = storage.getStoredAccessToken()!!
                var quantity = 0
                try {
                    quantity = editQuantity.text.toString().toInt()
                } catch (e: NumberFormatException) {}

                val item = Item(
                    null,
                    editTitle.text.toString(),
                    editCategory.text.toString(),
                    editDescription.text.toString(),
                    editBarcode.text.toString(),
                    quantity
                )
                barkoderApi.postItem(item, "bearer ${token.accessToken}")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this@ScanCodeActivity, "Item added", Toast.LENGTH_LONG).show()
                        editTitle.setText("")
                        editCategory.setText("")
                        editDescription.setText("")
                        editBarcode.setText("")
                        editQuantity.setText("")
                    }, { e ->
                        e.printStackTrace()
                        Toast.makeText(this@ScanCodeActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    })
            }
            else {
                if (editTitle.text.isEmpty()) editTitle.error = "Title cannot be empty"
                if (editBarcode.text.isEmpty()) editBarcode.error = "Barcode cannot be empty"
                if (editQuantity.text.isEmpty()) editQuantity.error = "Quantity cannot be empty"
            }
        }
    }
}
