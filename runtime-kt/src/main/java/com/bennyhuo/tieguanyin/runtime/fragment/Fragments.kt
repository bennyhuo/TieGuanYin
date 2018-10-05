package com.bennyhuo.tieguanyin.runtime.fragment

import android.support.v4.app.Fragment

fun Fragment.remove(){
    activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
}

fun Fragment.show(){
    activity?.supportFragmentManager?.beginTransaction()?.show(this)?.commit()
}

fun Fragment.hide(){
    activity?.supportFragmentManager?.beginTransaction()?.hide(this)?.commit()
}

fun Fragment.attach(){
    activity?.supportFragmentManager?.beginTransaction()?.attach(this)?.commit()
}

fun Fragment.detach(){
    activity?.supportFragmentManager?.beginTransaction()?.detach(this)?.commit()
}
