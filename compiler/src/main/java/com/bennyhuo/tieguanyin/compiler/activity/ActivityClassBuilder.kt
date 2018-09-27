package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.compiler.activity.methods.*
import com.bennyhuo.tieguanyin.compiler.basic.BasicClassBuilder
import com.squareup.javapoet.TypeSpec.Builder
import com.squareup.kotlinpoet.FileSpec

class ActivityClassBuilder(private val activityClass: ActivityClass): BasicClassBuilder(activityClass) {

    override fun buildCommon(typeBuilder: Builder) {
        ConstantBuilder(activityClass).build(typeBuilder)
        InjectMethodBuilder(activityClass).build(typeBuilder)
        SaveStateMethodBuilder(activityClass).build(typeBuilder)

        activityClass.activityResultClass?.buildOnActivityResultListenerInterface()?.let(typeBuilder::addType)
    }

    override fun buildKotlinBuilders(fileBuilder: FileSpec.Builder) {
        StartKotlinFunctionBuilder(activityClass).build(fileBuilder)
        FinishKotlinFunctionBuilder(activityClass).build(fileBuilder)
        activityClass.activityResultClass?.buildFinishWithResultKt()?.let(fileBuilder::addFunction)    }

    override fun buildJavaBuilders(typeBuilder: Builder) {
        StartMethodBuilder(activityClass).build(typeBuilder)
        FinishMethodBuilder(activityClass).build(typeBuilder)

        activityClass.activityResultClass?.buildFinishWithResultMethod()?.let(typeBuilder::addMethod)
    }

    companion object {
        const val METHOD_NAME = "start"
        const val METHOD_NAME_NO_OPTIONAL = METHOD_NAME + "WithoutOptional"
        const val METHOD_NAME_FOR_OPTIONAL = METHOD_NAME + "WithOptional"
        const val METHOD_NAME_FOR_OPTIONALS = METHOD_NAME + "WithOptionals"
        const val CONSTS_RESULT_PREFIX = "RESULT_"
    }
}