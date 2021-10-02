package com.bennyhuo.tieguanyin.compiler.ksp.basic.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.utils.camelToUnderline
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

abstract class BasicConstantBuilder(private val basicClass: BasicClass) {
    open fun build(typeBuilder: TypeSpec.Builder) {
        basicClass.fields.forEach { field ->
            typeBuilder.addField(FieldSpec.builder(String::class.java,
                    field.prefix + field.name.camelToUnderline(),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", field.name)
                    .build())
        }
    }
}