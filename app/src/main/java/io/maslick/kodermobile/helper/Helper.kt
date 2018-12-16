package io.maslick.kodermobile.helper

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.view.View
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

object AsyncHelper {
    @SuppressLint("CheckResult")
    fun <T> asyncRxExecutor(heavyFunction: () -> T, response : (response : T?) -> Unit) {
        val observable = Single.create<T> { e ->
            e.onSuccess(heavyFunction())
        }
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t: T? ->
                response(t)
            }
    }

    inline fun uiThreadExecutor(crossinline block: () -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post{
            block()
        }
    }
}

object Helper {
    @SuppressLint("SimpleDateFormat")
    fun Calendar.formatDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return formatter.format(this.time)
    }

    fun View.showSnackBar(message: String, duration: Int) {
        Snackbar.make(this, message, duration).show()
    }
}