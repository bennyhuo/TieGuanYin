package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.compiler.activity.builder.*
import com.bennyhuo.tieguanyin.compiler.basic.BasicClassBuilder
import com.bennyhuo.tieguanyin.compiler.basic.builder.FieldBuilder
import com.squareup.javapoet.TypeSpec.Builder
import com.squareup.kotlinpoet.FileSpec

class ActivityClassBuilder(private val activityClass: ActivityClass) : BasicClassBuilder(activityClass) {

    override fun buildCommon(typeBuilder: Builder) {
        ConstantBuilder(activityClass).build(typeBuilder)
        FieldBuilder(activityClass).build(typeBuilder)
        InjectMethodBuilder(activityClass).build(typeBuilder)
        SaveStateMethodBuilder(activityClass).build(typeBuilder)
    }

    override fun buildKotlinBuilders(fileBuilder: FileSpec.Builder) {
        StartKotlinFunctionBuilder(activityClass).build(fileBuilder)
        FinishKotlinFunctionBuilder(activityClass).build(fileBuilder)
    }

    override fun buildJavaBuilders(typeBuilder: Builder) {
        StartMethodBuilder(activityClass, METHOD_NAME).build(typeBuilder)
        FinishMethodBuilder(activityClass).build(typeBuilder)
    }

    companion object {
        const val METHOD_NAME = "start"
        const val CONSTS_RESULT_PREFIX = "RESULT_"
    }
}