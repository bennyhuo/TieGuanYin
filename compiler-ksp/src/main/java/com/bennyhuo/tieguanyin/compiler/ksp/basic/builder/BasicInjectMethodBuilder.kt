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
                basicClass.declaration.toKotlinTypeName()
            )
            .beginControlFlow("if(savedInstanceState != null)")

        for (field in basicClass.fields) {
            val name = field.name
            val key = field.key
            when (field) {
                is OptionalField -> {
                    val defaultValue = field.defaultValue
                    if (defaultValue == null) {
                        injectMethodBuilder
                            .addStatement(
                                "val %N: %T = %T.get(savedInstanceState, %S)",
                                name, field.asTypeName(), RUNTIME_UTILS.kotlin, key,
                            )
                            .beginControlFlow("if (%N != null)", name)
                            .addStatement("instance.%N = %N", name, name)
                            .endControlFlow()
                    } else {
                        injectMethodBuilder.addStatement(
                            "instance.%N = %T.get(savedInstanceState, %S, %L)",
                            name,
                            RUNTIME_UTILS.kotlin,
                            key,
                            defaultValue
                        )
                    }
                }
                !is OptionalField -> {
                    injectMethodBuilder.addStatement(
                        "instance.%L = %T.get(savedInstanceState, %S)",
                        name,
                        RUNTIME_UTILS.kotlin,
                        key
                    )
                }
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow()

        typeBuilder.addFunction(injectMethodBuilder.build())
    }
}