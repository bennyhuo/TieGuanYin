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
        defaultValue = when (symbol.type.kind) {
            TypeKind.BOOLEAN -> optional.booleanValue
            TypeKind.CHAR -> "'${optional.charValue}'"
            TypeKind.BYTE -> "(byte) ${optional.byteValue}"
            TypeKind.SHORT -> "(short) ${optional.shortValue}"
            TypeKind.INT -> optional.intValue
            TypeKind.LONG -> "${optional.longValue}L"
            TypeKind.FLOAT -> "${optional.floatValue}f"
            TypeKind.DOUBLE -> optional.doubleValue
            else -> if (symbol.type.isSameTypeWith(String::class)) {
                //注意字面量的引号
                """"${optional.stringValue}""""
            } else {
                null
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
