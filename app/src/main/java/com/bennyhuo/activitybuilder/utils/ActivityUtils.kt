@file:JvmName("ActivityUtils")
package com.bennyhuo.activitybuilder.utils

import android.app.Activity
import android.view.View
import android.widget.Toast
import com.bennyhuo.activitybuilder.JavaUtils

/**
 * Created by benny on 2/3/18.
 */

fun Activity.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun View.startJavaActivity(num: Int, isJava: Boolean ,
                           onJavaActivityResultListener: (String, Int)->Unit){
    JavaUtils.open(this.context, 1, false, onJavaActivityResultListener)
}