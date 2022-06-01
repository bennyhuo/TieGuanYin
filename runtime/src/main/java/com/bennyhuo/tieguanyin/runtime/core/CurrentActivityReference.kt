package com.bennyhuo.tieguanyin.runtime.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.bennyhuo.tieguanyin.runtime.utils.Logger
import java.lang.ref.WeakReference

/**
 * Created by benny at 2022/6/1 18:09.
 */
internal class CurrentActivityReference : Application.ActivityLifecycleCallbacks {
    private var currentActivityRef: WeakReference<Activity>? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivityRef = WeakReference(activity)
        performInject(activity, savedInstanceState)
        FragmentBuilder.onActivityCreated(activity)
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        performSaveState(activity, outState)
    }

    override fun onActivityDestroyed(activity: Activity) {
        FragmentBuilder.onActivityDestroyed(activity)
    }

    private fun performInject(activity: Activity, savedInstanceState: Bundle?) {
        try {
            val extras = savedInstanceState ?: activity.intent?.extras
            BuilderClassFinder.findBuilderClass(activity)
                    ?.getDeclaredMethod("inject", Activity::class.java, Bundle::class.java)
                    ?.invoke(null, activity, extras)
        } catch (e: Exception) {
            Logger.warn(e)
        }
    }

    private fun performSaveState(activity: Activity, outState: Bundle) {
        try {
            BuilderClassFinder.findBuilderClass(activity)
                    ?.getDeclaredMethod("saveState", Activity::class.java, Bundle::class.java)
                    ?.invoke(null, activity, outState)
        } catch (e: Exception) {
            Logger.warn(e)
        }
    }

    fun get() = currentActivityRef?.get()

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
}