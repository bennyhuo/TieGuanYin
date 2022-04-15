package com.bennyhuo.tieguanyin.compiler.ksp.basic.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

abstract class BasicSaveStateMethodBuilder(val basicClass: BasicClass) {

    abstract val instanceType: TypeName

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilder = FunSpec.builder("saveState")
            .addAnnotation(JvmStatic::class)
            .returns(UNIT)
            .addParameter("instance", instanceType)
            .addParameter("outState", BUNDLE.kotlin)
            .beginControlFlow(
                "if(instance is %T)",
                basicClass.declaration.toKotlinTypeName()
            )

        methodBuilder.addStatement("val intent = %T()", INTENT.kotlin)

        for (requiredField in basicClass.fields) {
            methodBuilder.addStatement("intent.putExtra(%S, instance.%L)",
                requiredField.key, requiredField.name
            )
        }

        methodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow()

        typeBuilder.addFunction(methodBuilder.build())
    }

}