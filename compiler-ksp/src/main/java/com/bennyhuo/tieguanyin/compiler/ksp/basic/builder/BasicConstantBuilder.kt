package com.bennyhuo.tieguanyin.compiler.ksp.basic.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.utils.camelToUnderline
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

abstract class BasicConstantBuilder(private val basicClass: BasicClass) {
    open fun build(typeBuilder: TypeSpec.Builder) {
        basicClass.fields.forEach { field ->
            typeBuilder.addProperty(
                PropertySpec.builder(
                    field.prefix + field.name.camelToUnderline(),
                    String::class, KModifier.CONST
                ).initializer("%S", field.name).build()
            )
        }
    }
}