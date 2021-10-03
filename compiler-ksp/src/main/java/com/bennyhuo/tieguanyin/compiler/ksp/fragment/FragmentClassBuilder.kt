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
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class FragmentClassBuilder(private val fragmentClass: FragmentClass): BasicClassBuilder(fragmentClass) {

    override fun buildCommon(typeBuilder: TypeSpec.Builder) {
        val companionObject = TypeSpec.companionObjectBuilder()
        ConstantBuilder(fragmentClass).build(companionObject)
        FieldBuilder(fragmentClass).build(typeBuilder, companionObject)
        InjectMethodBuilder(fragmentClass).build(companionObject)
        SaveStateMethodBuilder(fragmentClass).build(companionObject)
        typeBuilder.addType(companionObject.build())
    }

    override fun buildKotlinBuilders(fileBuilder: FileSpec.Builder) {
        ReplaceKFunctionBuilder(fragmentClass).build(fileBuilder)
        AddKFunctionBuilder(fragmentClass).build(fileBuilder)
    }

    override fun buildJavaBuilders(typeBuilder: TypeSpec.Builder) {
        ReplaceMethodBuilder(fragmentClass).build(typeBuilder)
        AddMethodBuilder(fragmentClass).build(typeBuilder)
    }
}