package com.bennyhuo.tieguanyin.compiler.fragment.methods

import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.utils.Utils
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class ConstantBuilder(private val fragmentClass: FragmentClass) {
    fun build(typeBuilder: TypeSpec.Builder) {
        fragmentClass.requiredFieldsRecursively.forEach { field ->
            typeBuilder.addField(FieldSpec.builder(String::class.java,
                    field.prefix + Utils.camelToUnderline(field.name),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", field.name)
                    .build())
        }
    }
}