package com.bennyhuo.tieguanyin.compiler.basic.entity

import com.bennyhuo.aptutils.types.isSameTypeWith
import com.bennyhuo.tieguanyin.annotations.Optional
import com.sun.tools.javac.code.Symbol
import javax.lang.model.type.TypeKind

/**
 * Created by benny on 1/31/18.
 */

class OptionalField(symbol: Symbol.VarSymbol) : Field(symbol) {

    var defaultValue: Any? = null
        private set

    override val prefix = "OPTIONAL_"

    init {
        val optional = symbol.getAnnotation(Optional::class.java)
        when (symbol.type.kind) {
            TypeKind.BOOLEAN -> defaultValue = optional.booleanValue
            TypeKind.BYTE, TypeKind.SHORT, TypeKind.INT, TypeKind.LONG, TypeKind.CHAR -> defaultValue = optional.intValue
            TypeKind.FLOAT, TypeKind.DOUBLE -> defaultValue = optional.floatValue
            else -> if (symbol.type.isSameTypeWith(String::class)) {
                //注意字面量的引号
                defaultValue = """"${optional.stringValue }""""
            }
        }
    }

    override fun asKotlinTypeName() = super.asKotlinTypeName().asNullable()

    override fun compareTo(other: Field): Int {
        return if (other is OptionalField) {
            super.compareTo(other)
        } else {
            //如果与 RequiredField 比较，Optional 永远排在后面
            1
        }
    }
}
