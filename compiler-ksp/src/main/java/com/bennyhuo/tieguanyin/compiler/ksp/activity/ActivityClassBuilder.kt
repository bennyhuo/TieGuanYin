package com.bennyhuo.tieguanyin.compiler.ksp.activity

import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.ConstantBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.FinishKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.FinishMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.InjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.NewIntentKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.NewIntentMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.SaveStateMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.StartKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.StartMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClassBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.FieldBuilder
import com.squareup.javapoet.TypeSpec.Builder
import com.squareup.kotlinpoet.FileSpec

class ActivityClassBuilder(private val activityClass: ActivityClass) : BasicClassBuilder(activityClass) {

    override fun buildCommon(typeBuilder: Builder) {
        ConstantBuilder(activityClass).build(typeBuilder)
        FieldBuilder(activityClass).build(typeBuilder)
        InjectMethodBuilder(activityClass).build(typeBuilder)
        SaveStateMethodBuilder(activityClass).build(typeBuilder)
        NewIntentMethodBuilder(activityClass).build(typeBuilder)
    }

    override fun buildKotlinBuilders(fileBuilder: FileSpec.Builder) {
        StartKFunctionBuilder(activityClass).build(fileBuilder)
        FinishKFunctionBuilder(activityClass).build(fileBuilder)
        NewIntentKFunctionBuilder(activityClass).build(fileBuilder)
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