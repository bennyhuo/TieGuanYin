package com.bennyhuo.tieguanyin.compiler.ksp.fragment

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClassBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.FieldBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.AddKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.AddMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.ConstantBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.InjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.ReplaceKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.ReplaceMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.SaveStateMethodBuilder
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
        AddKFunctionBuilder(fragmentClass).build(fileBuilder)
    }

    override fun buildJavaBuilders(typeBuilder: Builder) {
        ReplaceMethodBuilder(fragmentClass).build(typeBuilder)
        AddMethodBuilder(fragmentClass).build(typeBuilder)
    }
}