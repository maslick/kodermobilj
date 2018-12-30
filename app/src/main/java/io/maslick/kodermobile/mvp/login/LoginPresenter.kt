package io.maslick.kodermobile.mvp.login

import android.annotation.SuppressLint
import android.net.Uri
import io.maslick.kodermobile.Config
import io.maslick.kodermobile.helper.UriHelper
import io.maslick.kodermobile.oauth.IOAuth2AccessTokenStorage
import io.maslick.kodermobile.rest.IKeycloakRest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class LoginPresenter(val api: IKeycloakRest, val storage: IOAuth2AccessTokenStorage) : LoginContract.Presenter {
    override lateinit var view: LoginContract.View
    override fun start() {}

    @SuppressLint("CheckResult")
    override fun authenticate(uri: String?): Boolean {
        var returnFromAuth = false

        if (uri != null && uri.startsWith(Config.redirectUri)) {
            val code = UriHelper.splitQuery(uri)["code"]!!
            view.hideAll()
            returnFromAuth = true
            exchangeCodeForToken(code)
        }

        return returnFromAuth
    }

    override fun authUrl(): Uri {
        return Uri.parse(Config.authenticationCodeUrl)
            .buildUpon()
            .appendQueryParameter("client_id", Config.clientId)
            .appendQueryParameter("redirect_uri", Config.redirectUri)
            .appendQueryParameter("response_type", "code")
            .build()
    }

    @SuppressLint("CheckResult")
    private fun exchangeCodeForToken(code: String) {
        api.grantNewAccessToken(code, Config.clientId, Config.redirectUri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ token ->
                val expirationDate = Calendar.getInstance().clone() as Calendar
                val refreshExpirationDate = Calendar.getInstance().clone() as Calendar
                expirationDate.add(Calendar.SECOND, token.expiresIn!!)
                refreshExpirationDate.add(Calendar.SECOND, token.refreshExpiresIn!!)
                token.tokenExpirationDate = expirationDate
                token.refreshTokenExpirationDate = refreshExpirationDate
                storage.storeAccessToken(token)
                view.success()
            }, {
                it.printStackTrace()
                view.failure(it.message)
            })
    }
}