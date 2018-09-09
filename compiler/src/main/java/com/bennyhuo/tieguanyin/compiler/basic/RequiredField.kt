package com.bennyhuo.tieguanyin.compiler.basic

import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes
import com.squareup.javapoet.ClassName
import com.sun.tools.javac.code.Symbol

/**
 * Created by benny on 1/29/18.
 */

open class RequiredField(private val symbol: Symbol.VarSymbol) : Comparable<RequiredField> {
    val name = symbol.qualifiedName.toString()

    open val prefix = "REQUIRED_"

    val isPrivate = symbol.isPrivate

    val isPrimitive = symbol.type.isPrimitive

    fun asTypeName() = ClassName.get(symbol.type)
    fun asKotlinTypeName() = KotlinTypes.toKotlinType(symbol.type)

    override fun compareTo(requiredField: RequiredField): Int {
        return name.compareTo(requiredField.name)
    }
}
