package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

class OnIntentKFunctionBuilder(private val activityClass: ActivityClass) {

    fun build(typeBuilder: TypeSpec.Builder) {

        val type = LambdaTypeName.get(
            parameters = arrayOf(INTENT.kotlin),
            returnType = UNIT
        ).copy(nullable = true)

        val builderClassTypeName = ClassName(activityClass.packageName, activityClass.builderClassName)


        val onIntentFunBuilder = FunSpec.builder("onIntent")
            .addParameter(
                ParameterSpec.builder(
                    "block", type
                ).defaultValue("null").build()
            ).addStatement("this.onIntent = block")
            .addStatement("return this")
            .returns(builderClassTypeName)

        typeBuilder.addFunction(onIntentFunBuilder.build())
        typeBuilder.addProperty(
            PropertySpec.builder(
                "onIntent",
                type
            ).mutable(true).initializer("null").build()
        )
    }
}