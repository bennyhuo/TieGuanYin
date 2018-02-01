package com.bennyhuo.compiler.utils;

import com.squareup.javapoet.ClassName;

/**
 * Created by benny on 2/2/18.
 */

public class JavaTypes {
    public static final ClassName INTENT = ClassName.get("android.content", "Intent");
    public static final ClassName BUNDLE = ClassName.get("android.os", "Bundle");


    public static final ClassName RUNTIME_UTILS = ClassName.get("com.bennyhuo.utils", "Utils");


}
