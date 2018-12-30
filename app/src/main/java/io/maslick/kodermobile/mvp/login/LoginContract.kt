package io.maslick.kodermobile.mvp.login

import android.net.Uri
import io.maslick.kodermobile.mvp.BasePresenter
import io.maslick.kodermobile.mvp.BaseView

interface LoginContract {
    interface View : BaseView<Presenter> {
        fun initViews()
        fun hideAll()
        fun hideLoginButton()
        fun success()
        fun failure(message: String?)
    }

    interface Presenter : BasePresenter<View> {
        fun authUrl(): Uri
        fun authenticate(uri: Uri?): Boolean
    }
}