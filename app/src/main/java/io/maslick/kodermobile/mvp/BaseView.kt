package io.maslick.kodermobile.mvp

interface BaseView<out T : BasePresenter<*>> {
    val presenter: T
}
