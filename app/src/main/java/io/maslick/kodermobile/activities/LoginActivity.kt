package io.maslick.kodermobile.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import io.maslick.kodermobile.Config.authenticationCodeUrl
import io.maslick.kodermobile.Config.clientId
import io.maslick.kodermobile.Config.redirectUri
import io.maslick.kodermobile.R
import io.maslick.kodermobile.di.IKeycloakRest
import io.maslick.kodermobile.di.KeycloakToken
import io.maslick.kodermobile.helper.AsyncHelper
import io.maslick.kodermobile.helper.Helper.isTokenExpired
import io.maslick.kodermobile.storage.IOAuth2AccessTokenStorage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.HttpUrl
import org.koin.android.ext.android.inject
import java.util.*


class LoginActivity : RxAppCompatActivity() {

    private val keycloakApi: IKeycloakRest by inject()
    private val storage by inject<IOAuth2AccessTokenStorage>()

    private val authCodeUrl = Uri.parse(authenticationCodeUrl)
        .buildUpon()
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("response_type", "code")
        .build()
        .toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        checkTokenInStorage()
        initAuth()
    }

    private fun checkTokenInStorage() {
        val accessToken = storage.getStoredAccessToken()
        if (accessToken == null) return
        else if (!isTokenExpired(accessToken)) {
            setResult(RESULT_OK)
            finish()
        }
        else
            handleRefreshToken(accessToken.refreshToken!!).subscribe(handleSuccess(), handleError())
    }

    @SuppressLint("SetJavaScriptEnabled", "CheckResult")
    private fun initAuth() {
        webView.settings.userAgentString = "Barkoder/0.1 Android app"
        webView.settings.javaScriptEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        clearCookies()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url.startsWith(redirectUri)) {
                    AsyncHelper.uiThreadExecutor { webView.visibility = View.GONE }
                    keycloakApi.grantNewAccessToken(HttpUrl.parse(url)!!.queryParameter("code")!!, clientId, redirectUri)
                        .subscribeOn(Schedulers.io())
                        .compose(bindToLifecycle())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(handleSuccess(), handleError())
                }
            }
        }

        webView.loadUrl(authCodeUrl)
    }

    private fun clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

    private fun handleSuccess(): Consumer<KeycloakToken> {
        return Consumer { token ->
            val expirationDate = Calendar.getInstance().clone() as Calendar
            expirationDate.add(Calendar.SECOND, token.expiresIn!!)
            token.expirationDate = expirationDate
            storage.storeAccessToken(token)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun handleError(): Consumer<Throwable> {
        return Consumer {
            it.printStackTrace()
            Toast.makeText(this@LoginActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun handleRefreshToken(refreshToken: String): Observable<KeycloakToken> {
        return keycloakApi.refreshAccessToken(refreshToken, clientId)
            .compose(bindToLifecycle())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
