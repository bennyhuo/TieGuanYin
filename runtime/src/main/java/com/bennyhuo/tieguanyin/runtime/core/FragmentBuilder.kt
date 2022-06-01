package com.bennyhuo.tieguanyin.runtime.core

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentUtils
import com.bennyhuo.tieguanyin.runtime.utils.Logger

/**
 * Created by benny on 2/7/18.
 */
object FragmentBuilder {
    private val injector = FragmentInjector()

    fun onActivityCreated(activity: Activity) {
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(injector, true)
        }
    }

    fun onActivityDestroyed(activity: Activity) {
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(injector)
        }
    }

    fun <T : Fragment> showFragment(
            activity: FragmentActivity,
            isReplace: Boolean,
            containerId: Int,
            tag: String?,
            args: Bundle?,
            fragmentCls: Class<T>,
            sharedElements: ArrayList<Pair<String, String>>?
    ): T? {
        try {
            val fragment = fragmentCls.newInstance()
            fragment.arguments = args
            val transaction = activity.supportFragmentManager.beginTransaction()
            if (isReplace) {
                transaction.replace(containerId, fragment, tag)
            } else {
                transaction.add(containerId, fragment, tag)
            }
            if (!sharedElements.isNullOrEmpty()) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    fragment.sharedElementEnterTransition = AutoTransition()
                }
                for (sharedElement in sharedElements) {
                    if (sharedElement.first != null) {
                        FragmentUtils.addSharedElement(transaction, sharedElement.first, sharedElement.second)
                    }
                }
            }
            transaction.commit()
            return fragment
        } catch (e: Exception) {
            Logger.error(e)
        }
        return null
    }
}