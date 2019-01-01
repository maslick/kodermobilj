package io.maslick.kodermobile.mvp.listItems

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.maslick.kodermobile.R
import io.maslick.kodermobile.helper.replaceFragmentInActivity
import io.maslick.kodermobile.helper.setupActionBar
import org.koin.android.ext.android.inject

class ItemsActivity : AppCompatActivity() {

    private val itemsFragment: ItemsFragment by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.items_act)

        setupActionBar(R.id.toolbar) {
            title = resources.getString(R.string.app_name)
        }

        supportFragmentManager.findFragmentById(R.id.contentFrame) as ItemsFragment? ?: itemsFragment.also {
            replaceFragmentInActivity(it, R.id.contentFrame)
        }
    }

    companion object {
        const val AUTHORIZATION_REQUEST_CODE = 2
    }
}
