package com.bennyhuo.tieguanyin.runtime.utils

import android.os.Bundle
import android.support.v4.app.Fragment
import com.bennyhuo.tieguanyin.runtime.core.FragmentBuilder
import com.bennyhuo.tieguanyin.runtime.core.OnFragmentCreateListener

/**
 * Created by benny on 2/9/18.
 */
const val AUTO_INJECT_FLAG = "com.bennyhuo.tieguanyin.runtime.autoinject"

fun <T: Fragment> T.autoInject(){
    this.arguments?.let {
        it.putBoolean(AUTO_INJECT_FLAG, true)
    }
    FragmentBuilder.INSTANCE.addOnFragmentCreateListener(object: OnFragmentCreateListener{
        override fun onFragmentCreated(fragment: Fragment, savedInstanceState: Bundle?) {
            if(this@autoInject == fragment
                    || (this.javaClass == fragment.javaClass && fragment.arguments?.getBoolean(AUTO_INJECT_FLAG) == true)){

                FragmentBuilder.INSTANCE.removeOnFragmentCreateListener(this)
            }
        }
    })
}