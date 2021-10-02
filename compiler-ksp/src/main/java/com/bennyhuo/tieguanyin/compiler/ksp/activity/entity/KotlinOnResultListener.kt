package com.bennyhuo.tieguanyin.compiler.ksp.activity.entity

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ON_ACTIVITY_RESULT_LISTENER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.RUNTIME_UTILS
import com.squareup.kotlinpoet.*
import java.util.*

class KotlinOnResultListener(private val activityClass: ActivityClass) {

    val typeName by lazy {
        activityClass.resultParameters.map { resultParameter ->
            ParameterSpec.builder(resultParameter.name, resultParameter.kotlinTypeName).build()
        }.let { LambdaTypeName.get(null, it, UNIT).copy(nullable = true) }
    }
    /**
     * @return literal name like "onSampleActivityResultListener"
     */
    val name = "on" + activityClass.simpleName + "ResultListener"

    /**
     * @return object: onSampleActivityResultListener{ override fun onResult(bundle: Bundle){ if(not null) invoke. } }
     */
    fun buildObject(): com.squareup.kotlinpoet.TypeSpec {
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
        onResultFunBuilderKt.addStatement("%L($statementBuilderKt)", *argsKt.toTypedArray())
        onResultFunBuilderKt.endControlFlow()

        return com.squareup.kotlinpoet.TypeSpec.anonymousClassBuilder()
                .superclass(ON_ACTIVITY_RESULT_LISTENER.kotlin)
                .addSuperclassConstructorParameter(name)
                .addFunction(onResultFunBuilderKt.build())
                .build()
    }
}