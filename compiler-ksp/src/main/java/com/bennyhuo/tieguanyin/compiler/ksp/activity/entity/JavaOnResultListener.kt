package com.bennyhuo.tieguanyin.compiler.ksp.activity.entity

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ON_ACTIVITY_RESULT_LISTENER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.RUNTIME_UTILS
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import java.util.ArrayList

class JavaOnResultListener(private val activityClass: ActivityClass) {

    /**
     * @return literal name like "onSampleActivityResultListener"
     */
    val name = "on" + activityClass.simpleName + "ResultListener"

    val typeName = ClassName(
        activityClass.packageName, activityClass.builderClassName,
        "On" + activityClass.simpleName + "ResultListener"
    )

    fun buildInterface(): TypeSpec {
        val interfaceOnResultMethodBuilder = FunSpec.builder("onResult")
            .addModifiers(KModifier.ABSTRACT)
            .returns(UNIT)

        activityClass.resultParameters.forEach { resultParameter ->
            interfaceOnResultMethodBuilder.addParameter(
                resultParameter.name,
                resultParameter.kotlinTypeName
            )
        }

        return TypeSpec.interfaceBuilder(typeName)
            .addModifiers(KModifier.FUN)
            .addFunction(interfaceOnResultMethodBuilder.build())
            .build()
    }

    /**
     * @return object: onSampleActivityResultListener{ override fun onResult(bundle: Bundle){ if(not null) invoke. } }
     */
    fun buildObject(): TypeSpec {
        val onResultFunBuilderKt = FunSpec.builder("onResult")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("bundle", BUNDLE.kotlin)
            .returns(UNIT)

        onResultFunBuilderKt.beginControlFlow("if(%L != null)", name)
        val statementBuilderKt = StringBuilder()
        val argsKt = ArrayList<Any>()
        argsKt.add(name)

        activityClass.resultParameters.forEach { resultParameter ->
            statementBuilderKt.append("%T.get(bundle, %S),")
            argsKt.add(RUNTIME_UTILS.kotlin)
            argsKt.add(resultParameter.name)
        }
        if (statementBuilderKt.isNotEmpty()) {
            statementBuilderKt.deleteCharAt(statementBuilderKt.length - 1)
        }
        onResultFunBuilderKt.addStatement("%L.onResult($statementBuilderKt)", *argsKt.toTypedArray())
        onResultFunBuilderKt.endControlFlow()

        return TypeSpec.anonymousClassBuilder()
            .superclass(ON_ACTIVITY_RESULT_LISTENER.kotlin)
            .addSuperclassConstructorParameter(name)
            .addFunction(onResultFunBuilderKt.build())
            .build()
    }
}