package com.bennyhuo.tieguanyin.compiler.ksp.basic.entity

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Created by benny on 1/29/18.
 */

open class Field(private val declaration: KSPropertyDeclaration) : Comparable<Field> {
    val name = declaration.simpleName.asString()

    open val prefix = "REQUIRED_"

    val isPrivate = Modifier.PRIVATE in declaration.modifiers

    fun asTypeName() = asKotlinTypeName()

    open fun asKotlinTypeName() = declaration.type.resolve().toTypeName()

    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }
}
