package com.bennyhuo.tieguanyin.compiler.utils;

import com.squareup.javapoet.ClassName;

/**
 * Created by benny on 2/2/18.
 */

public class JavaTypes {
    public static final ClassName INTENT = ClassName.get("android.content", "Intent");
    public static final ClassName BUNDLE = ClassName.get("android.os", "Bundle");
    public static final ClassName ACTIVITY = ClassName.get("android.app", "Activity");
    public static final ClassName CONTEXT = ClassName.get("android.content", "Context");

    public static final ClassName SUPPORT_FRAGMENT = ClassName.get("android.support.v4.app", "Fragment");
    public static final ClassName SUPPORT_ACTIVITY = ClassName.get("android.support.v4.app", "FragmentActivity");

    public static final ClassName ON_ACTIVITY_RESULT_LISTENER = ClassName.get("com.bennyhuo.tieguanyin.runtime.core", "OnActivityResultListener");

    public static final ClassName RUNTIME_UTILS = ClassName.get("com.bennyhuo.tieguanyin.runtime.utils", "BundleUtils");

    public static final ClassName ACTIVITY_BUILDER = ClassName.get("com.bennyhuo.tieguanyin.runtime.core", "ActivityBuilder");
    public static final ClassName FRAGMENT_BUILDER = ClassName.get("com.bennyhuo.tieguanyin.runtime.core", "FragmentBuilder");
    public static final ClassName ON_ACTIVITY_CREATE_LISTENER = ClassName.get("com.bennyhuo.tieguanyin.runtime.core", "OnActivityCreateListener");
    public static final ClassName ON_FRAGMENT_CREATE_LISTENER = ClassName.get("com.bennyhuo.tieguanyin.runtime.core", "OnFragmentCreateListener");

    public static final ClassName ARRAY_LIST = ClassName.get("java.util", "ArrayList");
    public static final ClassName SUPPORT_PAIR = ClassName.get("android.support.v4.util", "Pair");
    public static final ClassName VIEW = ClassName.get("android.view", "View");
    public static final ClassName VIEW_COMPAT = ClassName.get("android.support.v4.view", "ViewCompat");
    public static final ClassName ACTIVITY_COMPAT = ClassName.get("android.support.v4.app", "ActivityCompat");
}
