package com.bennyhuo.tieguanyin.compiler.basic.entity

import com.bennyhuo.aptutils.types.asKotlinTypeName
import com.squareup.javapoet.ClassName
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.lang.model.type.PrimitiveType

/**
 * Created by benny on 1/29/18.
 */

open class Field(private val element: VariableElement) : Comparable<Field> {
    val name = element.simpleName.toString()

    open val prefix = "REQUIRED_"

    val isPrivate = Modifier.PRIVATE in element.modifiers

    val isPrimitive = element.asType() is PrimitiveType

    fun asTypeName() = ClassName.get(element.asType())

    open fun asKotlinTypeName() = element.asType().asKotlinTypeName()

    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }
}
