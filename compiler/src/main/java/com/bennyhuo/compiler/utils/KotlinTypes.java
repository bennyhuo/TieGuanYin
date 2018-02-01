package com.bennyhuo.compiler.utils;


import com.squareup.kotlinpoet.ClassName;

/**
 * Created by benny on 2/2/18.
 */

public class KotlinTypes {
    public static final ClassName BUNDLE = new ClassName("android.os", "Bundle");
    public static final ClassName INTENT = new ClassName("android.content", "Intent");
}
