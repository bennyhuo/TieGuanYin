package com.bennyhuo.tieguanyin.compiler.basic.entity

import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes
import com.squareup.javapoet.ClassName
import com.sun.tools.javac.code.Symbol

/**
 * Created by benny on 1/29/18.
 */

open class Field(private val symbol: Symbol.VarSymbol) : Comparable<Field> {
    val name = symbol.qualifiedName.toString()

    open val prefix = "REQUIRED_"

    val isPrivate = symbol.isPrivate

    val isPrimitive = symbol.type.isPrimitive

    fun asTypeName() = ClassName.get(symbol.type)

    open fun asKotlinTypeName() = KotlinTypes.toKotlinType(symbol.type)

    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }
}
