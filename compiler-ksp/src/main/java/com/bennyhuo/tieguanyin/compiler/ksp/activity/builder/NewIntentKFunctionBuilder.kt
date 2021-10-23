package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec

class NewIntentKFunctionBuilder(private val activityClass: ActivityClass) {

    fun build(builder: FileSpec.Builder) {
        val newIntentFunBuilder = FunSpec.builder("processNewIntent")
                .receiver(activityClass.declaration.toKotlinTypeName())
                .addParameter("intent", INTENT.kotlin.copy(nullable = true))
                .addParameter(ParameterSpec.builder("updateIntent", Boolean::class.java).defaultValue("true").build())

        newIntentFunBuilder.addStatement("%L.processNewIntent(this, intent, updateIntent)", activityClass.builderClassName)
        builder.addFunction(newIntentFunBuilder.build())
    }
}