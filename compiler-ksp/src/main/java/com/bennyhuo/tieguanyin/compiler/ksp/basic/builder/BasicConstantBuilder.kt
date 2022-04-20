package com.bennyhuo.tieguanyin.compiler.ksp.basic.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.utils.camelToUnderline
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.*

abstract class BasicConstantBuilder(private val basicClass: BasicClass) {
    open fun build(typeBuilder: TypeSpec.Builder) {
        basicClass.fields.forEach { field ->
            typeBuilder.addProperty(
                PropertySpec.builder(
                    field.prefix + field.name.camelToUnderline().uppercase(Locale.getDefault()),
                    String::class, KModifier.CONST
                ).initializer("%S", field.key).build()
            )
        }
    }
}