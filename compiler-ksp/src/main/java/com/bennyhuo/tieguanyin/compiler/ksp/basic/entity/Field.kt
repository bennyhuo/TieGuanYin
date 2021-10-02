package com.bennyhuo.tieguanyin.compiler.ksp.basic.entity

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.javapoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Created by benny on 1/29/18.
 */

open class Field(private val symbol: KSPropertyDeclaration) : Comparable<Field> {
    val name = symbol.simpleName.asString()

    open val prefix = "REQUIRED_"

    val isPrivate = Modifier.PRIVATE in symbol.modifiers

    fun asTypeName() = ClassName.bestGuess(symbol.type.resolve().declaration.qualifiedName!!.asString())

    open fun asKotlinTypeName() = symbol.type.resolve().toTypeName()

    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }
}
