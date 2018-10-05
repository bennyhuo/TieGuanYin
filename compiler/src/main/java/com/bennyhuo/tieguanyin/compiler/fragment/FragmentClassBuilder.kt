package com.bennyhuo.tieguanyin.compiler.fragment

import com.bennyhuo.tieguanyin.compiler.basic.BasicClassBuilder
import com.bennyhuo.tieguanyin.compiler.basic.builder.FieldBuilder
import com.bennyhuo.tieguanyin.compiler.fragment.builder.*
import com.squareup.javapoet.TypeSpec.Builder
import com.squareup.kotlinpoet.FileSpec

class FragmentClassBuilder(private val fragmentClass: FragmentClass): BasicClassBuilder(fragmentClass) {

    override fun buildCommon(typeBuilder: Builder) {
        ConstantBuilder(fragmentClass).build(typeBuilder)
        FieldBuilder(fragmentClass).build(typeBuilder)
        InjectMethodBuilder(fragmentClass).build(typeBuilder)
        SaveStateMethodBuilder(fragmentClass).build(typeBuilder)
    }

    override fun buildKotlinBuilders(fileBuilder: FileSpec.Builder) {
        ReplaceKFunctionBuilder(fragmentClass).build(fileBuilder)
    }

    override fun buildJavaBuilders(typeBuilder: Builder) {
        ReplaceMethodBuilder(fragmentClass).build(typeBuilder)
    }
}