package com.bennyhuo.tieguanyin.compiler.ksp.basic.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.RUNTIME_UTILS
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

abstract class BasicInjectMethodBuilder(val basicClass: BasicClass) {

    abstract val instanceType: TypeName

    fun build(typeBuilder: TypeSpec.Builder) {
        val injectMethodBuilder = FunSpec.builder("inject")
            .addParameter("instance", instanceType)
            .addParameter("savedInstanceState", BUNDLE.kotlin.copy(nullable = true))
            .addAnnotation(JvmStatic::class)
            .returns(UNIT)
            .beginControlFlow(
                "if(instance is %T)",
                basicClass.typeElement.toKotlinTypeName()
            )
            .beginControlFlow("if(savedInstanceState != null)")

        for (field in basicClass.fields) {
            val name = field.name
            when (field) {
                is OptionalField -> {
                    injectMethodBuilder.addStatement(
                        "instance.%L = %T.get(savedInstanceState, %S, %L)",
                        name,
                        RUNTIME_UTILS.kotlin,
                        name,
                        field.defaultValue
                    )
                }
                !is OptionalField -> {
                    injectMethodBuilder.addStatement(
                        "instance.%L = %T.get(savedInstanceState, %S)",
                        name,
                        RUNTIME_UTILS.kotlin,
                        name
                    )
                }
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow()

        typeBuilder.addFunction(injectMethodBuilder.build())
    }
}