package com.bennyhuo.tieguanyin.runtime.core

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bennyhuo.tieguanyin.runtime.utils.Logger

/**
 * Created by benny at 2022/6/1 18:18.
 */
class FragmentInjector : FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)
        performInject(f, savedInstanceState)
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        super.onFragmentSaveInstanceState(fm, f, outState)
        performSaveState(f, outState)
    }

    private fun performInject(fragment: Fragment, savedInstanceState: Bundle?) {
        try {
            val arguments = savedInstanceState ?: fragment.arguments ?: return
            BuilderClassFinder.findBuilderClass(fragment)
                    ?.getDeclaredMethod("inject", Fragment::class.java, Bundle::class.java)
                    ?.invoke(null, fragment, arguments)
            Logger.debug("inject success: fragment=$fragment, state=$arguments")
        } catch (e: Exception) {
            Logger.warn("inject failed: fragment=$fragment, state=$savedInstanceState, e=$e")
        }
    }

    private fun performSaveState(fragment: Fragment, outState: Bundle) {
        try {
            BuilderClassFinder.findBuilderClass(fragment)
                    ?.getDeclaredMethod("saveState", Fragment::class.java, Bundle::class.java)
                    ?.invoke(null, fragment, outState)
            Logger.debug("save success: fragment=$fragment, state=$outState")
        } catch (e: Exception) {
            Logger.warn("save failed: fragment=$fragment, state=$outState, e=$e")
        }
    }
}