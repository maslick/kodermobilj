package io.maslick.kodermobile.mvp.addEditItem

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.maslick.kodermobile.R
import io.maslick.kodermobile.di.Properties.EDIT_ITEM_ID
import io.maslick.kodermobile.helper.replaceFragmentInActivity
import io.maslick.kodermobile.helper.setupActionBar
import io.maslick.kodermobile.rest.Item
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.property
import org.koin.android.ext.android.setProperty

class AddEditItemActivity : AppCompatActivity() {

    private val addEditItemFragment: AddEditItemFragment by inject()
    private val selectedItem by property(EDIT_ITEM_ID, Item())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.additem_act)

        setProperty(EDIT_ITEM_ID, selectedItem)

        setupActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if (selectedItem.id == null) "Add new Item" else "Edit Item"
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
        const val ADD_ITEM_REQUEST_CODE = 1
    }
}
