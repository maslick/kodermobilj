package io.maslick.kodermobile.mvp.addEditItem

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.maslick.kodermobile.R
import io.maslick.kodermobile.helper.replaceFragmentInActivity
import io.maslick.kodermobile.helper.setupActionBar
import org.koin.android.ext.android.inject

class AddEditItemActivity : AppCompatActivity() {

    private val addEditItemFragment: AddEditItemFragment by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.additem_act)

        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Add new Item"
        }

        supportFragmentManager.findFragmentById(R.id.addEditItemContentFrame) as AddEditItemFragment? ?: addEditItemFragment.also {
            replaceFragmentInActivity(it, R.id.addEditItemContentFrame)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val REQUEST_ADD_ITEM = 1
    }
}
