package io.maslick.kodermobile.oauth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.maslick.kodermobile.Config.authenticationCodeUrl
import io.maslick.kodermobile.Config.clientId
import io.maslick.kodermobile.Config.redirectUri
import io.maslick.kodermobile.R
import io.maslick.kodermobile.rest.IKeycloakRest
import io.maslick.kodermobile.rest.KeycloakToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import java.util.*


class LoginActivity : RxAppCompatActivity() {

    private val api by inject<IKeycloakRest>()
    private val storage by inject<IOAuth2AccessTokenStorage>()

    private var authenticated = false

    private val authCodeUrl = Uri.parse(authenticationCodeUrl)
        .buildUpon()
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("response_type", "code")
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        println("creating view")
        val justAuthenticated = checkOAuthAndReturn(intent)
        if (!authenticated) initViews()
        login_button.setOnClickListener {
            if (!justAuthenticated) {
                login_text.visibility = View.VISIBLE
                login_button.visibility = View.GONE
                launchBrowser()
            }
        }
    }

    private fun initViews() {
        login_text.visibility = View.GONE
        login_button.visibility = View.VISIBLE
    }

    private fun hideAll() {
        login_text.visibility = View.GONE
        login_button.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent) {
        println("new intent")
        checkOAuthAndReturn(intent)
    }

    override fun onBackPressed() {}

    @SuppressLint("CheckResult")
    private fun checkOAuthAndReturn(intent: Intent): Boolean {
        var returnFromAuth = false
        val uri = intent.data

        if (uri != null && uri.toString().startsWith(redirectUri)) {
            hideAll()
            authenticated = true
            val code = uri.getQueryParameter("code")
            println(code)
            returnFromAuth = true
            api.grantNewAccessToken(code, clientId, redirectUri)
                .subscribeOn(Schedulers.io())
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleSuccess(), handleError())
        }

        return returnFromAuth
    }

    private fun launchBrowser() {
        startActivity(Intent(Intent.ACTION_VIEW, authCodeUrl))
    }

    private fun handleSuccess(): Consumer<KeycloakToken> {
        return Consumer { token ->
            val expirationDate = Calendar.getInstance().clone() as Calendar
            val refreshExpirationDate = Calendar.getInstance().clone() as Calendar
            expirationDate.add(Calendar.SECOND, token.expiresIn!!)
            refreshExpirationDate.add(Calendar.SECOND, token.refreshExpiresIn!!)
            token.tokenExpirationDate = expirationDate
            token.refreshTokenExpirationDate = refreshExpirationDate
            storage.storeAccessToken(token)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun handleError(): Consumer<Throwable> {
        return Consumer {
            it.printStackTrace()
            Toast.makeText(this@LoginActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
