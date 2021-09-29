package com.bennyhuo.tieguanyin.compiler.basic.types

import com.bennyhuo.aptutils.types.ClassType


/**
 * Created by benny on 2/2/18.
 */
val GENERATED_ANNOTATION = ClassType("com.bennyhuo.tieguanyin.annotations.Generated")

val INTENT = ClassType("android.content.Intent")
val BUNDLE = ClassType("android.os.Bundle")
val ACTIVITY = ClassType("android.app.Activity")
val CONTEXT = ClassType("android.content.Context")

val ON_ACTIVITY_RESULT_LISTENER = ClassType("com.bennyhuo.tieguanyin.runtime.core.OnActivityResultListener")

val RUNTIME_UTILS = ClassType("com.bennyhuo.tieguanyin.runtime.utils.BundleUtils")
val VIEW_UTILS = ClassType("com.bennyhuo.tieguanyin.runtime.utils.ViewUtils")

val ACTIVITY_BUILDER = ClassType("com.bennyhuo.tieguanyin.runtime.core.ActivityBuilder")
val FRAGMENT_BUILDER = ClassType("com.bennyhuo.tieguanyin.runtime.core.FragmentBuilder")
val ON_ACTIVITY_CREATE_LISTENER = ClassType("com.bennyhuo.tieguanyin.runtime.core.OnActivityCreateListener")
val ON_FRAGMENT_CREATE_LISTENER = ClassType("com.bennyhuo.tieguanyin.runtime.core.OnFragmentCreateListener")

val VIEW = ClassType("android.view.View")
val VIEW_GROUP = ClassType("android.view.ViewGroup")

val ARRAY_LIST = ClassType("java.util.ArrayList")
val HASH_MAP = ClassType("java.util.HashMap")

val STRING = ClassType("java.lang.String")

var useAndroidx: Boolean? = null
    set(value) {
        if (field == null) {
            field = value
        } else if(field != value) {
            if (field == true) {
                throw IllegalStateException("Androidx is enabled, but support fragments are found.")
            } else {
                throw IllegalStateException("Use support libraries, but androidx fragments are found.")
            }
        }
    }

val SUPPORT_PAIR = ClassType("android.support.v4.util.Pair")
val ANDROIDX_PAIR = ClassType("androidx.core.util.Pair")
val PAIR: ClassType
    get() = if (useAndroidx == true) ANDROIDX_PAIR else SUPPORT_PAIR


val SUPPORT_FRAGMENT = ClassType("android.support.v4.app.Fragment")
val ANDROIDX_FRAGMENT = ClassType("androidx.fragment.app.Fragment")
val FRAGMENT: ClassType
    get() = if (useAndroidx == true) ANDROIDX_FRAGMENT else SUPPORT_FRAGMENT


val SUPPORT_FRAGMENT_ACTIVITY = ClassType("android.support.v4.app.FragmentActivity")
val ANDROIDX_FRAGMENT_ACTIVITY = ClassType("androidx.fragment.app.FragmentActivity")
val FRAGMENT_ACTIVITY: ClassType
    get() = if (useAndroidx == true) ANDROIDX_FRAGMENT_ACTIVITY else SUPPORT_FRAGMENT_ACTIVITY


val SUPPORT_VIEW_COMPAT = ClassType("android.support.v4.view.ViewCompat")
val ANDROIDX_VIEW_COMPAT = ClassType("androidx.core.view.ViewCompat")
val VIEW_COMPAT: ClassType
    get() = if (useAndroidx == true) ANDROIDX_VIEW_COMPAT else SUPPORT_VIEW_COMPAT

val SUPPORT_ACTIVITY_COMPAT = ClassType("android.support.v4.app.ActivityCompat")
val ANDROIDX_ACTIVITY_COMPAT = ClassType("androidx.core.app.ActivityCompat")
val ACTIVITY_COMPAT: ClassType
    get() = if (useAndroidx == true) ANDROIDX_ACTIVITY_COMPAT else SUPPORT_ACTIVITY_COMPAT

