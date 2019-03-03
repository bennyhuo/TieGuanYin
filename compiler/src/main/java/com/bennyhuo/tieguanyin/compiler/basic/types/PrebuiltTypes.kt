package com.bennyhuo.tieguanyin.compiler.basic.types

import com.bennyhuo.aptutils.types.ClassType


/**
 * Created by benny on 2/2/18.
 */
val TIEGUANYIN = ClassType("com.bennyhuo.tieguanyin.runtime.Tieguanyin")

val GENERATED_ANNOTATION = ClassType("com.bennyhuo.tieguanyin.annotations.Generated")

val INTENT = ClassType("android.content.Intent")
val BUNDLE = ClassType("android.os.Bundle")
val ACTIVITY = ClassType("android.app.Activity")
val CONTEXT = ClassType("android.content.Context")

val SUPPORT_FRAGMENT = ClassType("android.support.v4.app.Fragment")
val SUPPORT_ACTIVITY = ClassType("android.support.v4.app.FragmentActivity")

val ON_ACTIVITY_RESULT_LISTENER = ClassType("com.bennyhuo.tieguanyin.runtime.core.OnActivityResultListener")

val RUNTIME_UTILS = ClassType("com.bennyhuo.tieguanyin.runtime.utils.BundleUtils")
val VIEW_UTILS = ClassType("com.bennyhuo.tieguanyin.runtime.utils.ViewUtils")

val ACTIVITY_BUILDER = ClassType("com.bennyhuo.tieguanyin.runtime.core.ActivityBuilder")
val FRAGMENT_BUILDER = ClassType("com.bennyhuo.tieguanyin.runtime.core.FragmentBuilder")
val ON_ACTIVITY_CREATE_LISTENER = ClassType("com.bennyhuo.tieguanyin.runtime.core.OnActivityCreateListener")
val ON_FRAGMENT_CREATE_LISTENER = ClassType("com.bennyhuo.tieguanyin.runtime.core.OnFragmentCreateListener")

val ARRAY_LIST = ClassType("java.util.ArrayList")
val SUPPORT_PAIR = ClassType("android.support.v4.util.Pair")
val VIEW = ClassType("android.view.View")
val VIEW_COMPAT = ClassType("android.support.v4.view.ViewCompat")
val ACTIVITY_COMPAT = ClassType("android.support.v4.app.ActivityCompat")

val HASH_MAP = ClassType("java.util.HashMap")

val VIEW_GROUP = ClassType("android.view.ViewGroup")
val FRAGMENT = ClassType("android.app.Fragment")

val STRING = ClassType("java.lang.String")

val PARCELABLE = ClassType("android.os.Parcelable")
val PARCELABLE_ARRAY = ClassType("android.os.Parcelable[]")
val SIZE = ClassType("android.util.Size")
val SIZEF = ClassType("android.util.SizeF")
