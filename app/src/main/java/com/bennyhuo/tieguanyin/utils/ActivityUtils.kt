@file:JvmName("ActivityUtils")
package com.bennyhuo.tieguanyin.utils

import android.app.Activity
import android.widget.Toast

/**
 * Created by benny on 2/3/18.
 */

fun Activity.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()