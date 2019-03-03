package com.bennyhuo.tieguanyin.compiler.basic.entity

import com.bennyhuo.aptutils.types.asKotlinTypeName
import com.bennyhuo.tieguanyin.compiler.basic.types.*
import com.squareup.javapoet.ClassName
import com.sun.tools.javac.code.Symbol

/**
 * Created by benny on 1/29/18.
 */

open class Field(protected val symbol: Symbol.VarSymbol) : Comparable<Field> {
    val name = symbol.qualifiedName.toString()

    open val prefix = "REQUIRED_"

    val isPrivate = symbol.isPrivate

    val isPrimitive = symbol.type.isPrimitive

    val isInternalType = SupportedTypes.isInternalType(symbol.type)

    val isAnnotatedType = DataType.isAnnotatedType(symbol.type)

    fun asTypeName() = ClassName.get(symbol.type)

    open fun asKotlinTypeName() = symbol.type.asKotlinTypeName()

    fun javaTemplateToBundle(receiver: String = "", suggestedGetterName: String = ""): Pair<String, Array<out Any>>{
        val getterName = if(suggestedGetterName.isEmpty()) {
            if(isPrivate) "get${name.capitalize()}()" else name
        } else suggestedGetterName
        val receiverDotGetter = if(receiver.isNotBlank()) "$receiver.$getterName" else getterName
        return if(isInternalType) {
            receiverDotGetter to emptyArray()
        } else if(isAnnotatedType){
            "\$T.<\$T, \$T>findProperConverter(\$T.class).convertFrom($receiverDotGetter)" to arrayOf(TIEGUANYIN.java, asTypeName(), BUNDLE.java, asTypeName())
        } else {
            throw UnsupportedOperationException("Unsupported type: ${symbol.enclosingElement}: ${symbol}")
        }
    }

    open fun javaTemplateFromBundle(bundleName: String, suggestedKey: String = ""): Pair<String, Array<out Any?>>{
        val key = if(suggestedKey.isEmpty()) name else suggestedKey
        return if(isInternalType){
            "\$T.<\$T>get($bundleName, \$S)" to arrayOf(RUNTIME_UTILS.java, asTypeName().box(), key)
        } else if(isAnnotatedType){
            "\$T.findProperConverter(\$T.class).convertTo(\$T.get($bundleName, \$S))" to arrayOf(TIEGUANYIN.java, asTypeName(), RUNTIME_UTILS.java, key)
        } else {
            throw UnsupportedOperationException("Unsupported type: ${symbol.enclosingElement}: ${symbol}")
        }
    }

    fun kotlinTemplateToBundle(receiver: String = "", suggestedGetterName: String = ""): Pair<String, Array<out Any>>{
        val getterName = if(suggestedGetterName.isEmpty()) {
            if(isPrivate) "get${name.capitalize()}()" else name
        } else suggestedGetterName
        val receiverDotGetter = if(receiver.isNotBlank()) "$receiver.$getterName" else getterName
        return if(isInternalType) {
            receiverDotGetter to emptyArray()
        } else if(isAnnotatedType){
            "%T.findProperConverter<%T, %T>(%T::class.java).convertFrom($receiverDotGetter)" to arrayOf(TIEGUANYIN.kotlin, asKotlinTypeName().asNonNullable(), BUNDLE.kotlin,  asKotlinTypeName().asNonNullable())
        } else {
            throw UnsupportedOperationException("Unsupported type: ${symbol.enclosingElement}: ${symbol}")
        }
    }


    override fun compareTo(other: Field): Int {
        return name.compareTo(other.name)
    }
}
