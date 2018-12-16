package io.maslick.kodermobile.mvp

interface BasePresenter<T> {
    fun start()
    var view: T
}