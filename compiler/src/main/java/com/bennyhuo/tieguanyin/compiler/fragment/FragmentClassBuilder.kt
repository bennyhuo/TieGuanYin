package com.bennyhuo.tieguanyin.compiler.fragment

import com.bennyhuo.tieguanyin.compiler.basic.BasicClassBuilder
import com.bennyhuo.tieguanyin.compiler.fragment.methods.*
import com.squareup.javapoet.TypeSpec.Builder
import com.squareup.kotlinpoet.FileSpec

class FragmentClassBuilder(private val fragmentClass: FragmentClass): BasicClassBuilder(fragmentClass) {

    override fun buildCommon(typeBuilder: Builder) {
        ConstantBuilder(fragmentClass).build(typeBuilder)
        InjectMethodBuilder(fragmentClass).build(typeBuilder)
        SaveStateMethodBuilder(fragmentClass).build(typeBuilder)
    }

    override fun buildKotlinBuilders(fileBuilder: FileSpec.Builder) {
        ShowKotlinFunctionBuilder(fragmentClass).build(fileBuilder)
    }

    override fun buildJavaBuilders(typeBuilder: Builder) {
        ShowMethodBuilder(fragmentClass).build(typeBuilder)
    }

    companion object {
        const val METHOD_NAME = "show"
        const val METHOD_NAME_NO_OPTIONAL = METHOD_NAME + "WithoutOptional"
        const val METHOD_NAME_FOR_OPTIONAL = METHOD_NAME + "WithOptional"
        const val METHOD_NAME_FOR_OPTIONALS = METHOD_NAME + "WithOptionals"
    }
}