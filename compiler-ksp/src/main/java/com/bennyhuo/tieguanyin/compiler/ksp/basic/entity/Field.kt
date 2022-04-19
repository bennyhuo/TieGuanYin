package com.bennyhuo.tieguanyin.compiler.ksp.basic.entity

import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Created by benny on 1/29/18.
 */

open class Field(
    key: String,
    private val declaration: KSPropertyDeclaration
) : Comparable<Field> {

    val name = declaration.simpleName.asString()

    val key = key.takeIf { it.isNotBlank() } ?: name

    val docString = declaration.docString?.replace("\n", "") ?: ""

    open val prefix = "REQUIRED_"

    init {
        checkValidation()
    }

    fun asTypeName() = asKotlinTypeName()

    open fun asKotlinTypeName() = declaration.type.resolve().toTypeName()

    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }

    private fun isProtectedOrPrivate(): Boolean {
        return Modifier.PRIVATE in declaration.modifiers || Modifier.PROTECTED in declaration.modifiers
    }

    private fun checkValidation() {
        if(isProtectedOrPrivate()) {
            logger.error("""Field '$name' in '${declaration.parentDeclaration?.qualifiedName?.asString()}'
                | should not be private or protected.""".trimMargin())
        }
    }
}
