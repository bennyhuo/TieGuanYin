package com.bennyhuo.tieguanyin.compiler.ksp.activity

import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.ConstantBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.FinishKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.FinishMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.InjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.NewIntentKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.NewIntentMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.OnIntentKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.SaveStateMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.StartKFunctionBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.builder.StartMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClassBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.FieldBuilder
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class ActivityClassBuilder(private val activityClass: ActivityClass) : BasicClassBuilder(activityClass) {

    override fun buildBuilderClass(typeBuilder: TypeSpec.Builder) {
        val companionObject = TypeSpec.companionObjectBuilder()
        ConstantBuilder(activityClass).build(companionObject)

        FieldBuilder(activityClass).build(typeBuilder, companionObject)
        InjectMethodBuilder(activityClass).build(companionObject)
        SaveStateMethodBuilder(activityClass).build(companionObject)
        NewIntentMethodBuilder(activityClass).build(companionObject)
        FinishMethodBuilder(activityClass).build(companionObject)
        OnIntentKFunctionBuilder(activityClass).build(typeBuilder)
        StartMethodBuilder(activityClass, METHOD_NAME).build(typeBuilder)

        typeBuilder.addType(companionObject.build())
    }

    override fun buildKotlinExtensions(fileBuilder: FileSpec.Builder) {
        StartKFunctionBuilder(activityClass).build(fileBuilder)
        FinishKFunctionBuilder(activityClass).build(fileBuilder)
        NewIntentKFunctionBuilder(activityClass).build(fileBuilder)
    }

    companion object {
        const val METHOD_NAME = "start"
        const val CONSTS_RESULT_PREFIX = "RESULT_"
    }
}