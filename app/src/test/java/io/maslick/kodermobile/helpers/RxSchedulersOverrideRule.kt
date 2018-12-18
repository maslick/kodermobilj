package io.maslick.kodermobile.helpers

import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.functions.Function
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Callable

open class RxSchedulersOverrideRule : TestRule {

    private val mRxAndroidSchedulersHook: Function<Callable<Scheduler>, Scheduler> = Function { Schedulers.trampoline() }
    private val mRxJavaImmediateScheduler: Function<Scheduler, Scheduler> = Function { Schedulers.trampoline() }

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                RxAndroidPlugins.reset()
                RxAndroidPlugins.setInitMainThreadSchedulerHandler(mRxAndroidSchedulersHook)

                RxJavaPlugins.reset()
                RxJavaPlugins.setIoSchedulerHandler(mRxJavaImmediateScheduler)
                RxJavaPlugins.setNewThreadSchedulerHandler(mRxJavaImmediateScheduler)

                base?.evaluate()

                RxAndroidPlugins.reset()
                RxJavaPlugins.reset()
            }
        }
    }

}