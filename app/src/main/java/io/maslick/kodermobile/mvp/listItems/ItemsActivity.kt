package io.maslick.kodermobile.mvp.listItems

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.maslick.kodermobile.R
import io.maslick.kodermobile.helper.Helper.isRefreshTokenExpired
import io.maslick.kodermobile.helper.replaceFragmentInActivity
import io.maslick.kodermobile.helper.setupActionBar
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.oauth.LoginActivity
import org.koin.android.ext.android.inject

class ItemsActivity : AppCompatActivity() {

    private val itemsFragment: ItemsFragment by inject()
    private val storage by inject<IOAuth2AccessTokenStorage>()

    private val AUTHORIZATION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.items_act)

        setupActionBar(R.id.toolbar) {
            title = resources.getString(R.string.app_name)
        }

        // handle authorization
        if (isRefreshTokenExpired(storage.getStoredAccessToken()))
            startActivityForResult(Intent(this, LoginActivity::class.java), AUTHORIZATION_REQUEST_CODE)

        supportFragmentManager.findFragmentById(R.id.contentFrame) as ItemsFragment? ?: itemsFragment.also {
            replaceFragmentInActivity(it, R.id.contentFrame)
        }
    }
}
