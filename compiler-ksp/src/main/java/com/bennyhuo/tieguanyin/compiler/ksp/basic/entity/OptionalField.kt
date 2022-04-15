package com.bennyhuo.tieguanyin.compiler.ksp.basic.entity

import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Modifier

/**
 * Created by benny on 1/31/18.
 */

class OptionalField(
    declaration: KSPropertyDeclaration, optional: Optional
) : Field(optional.key, declaration) {

    var defaultValue: Any?
        private set

    override val prefix = "OPTIONAL_"

    init {
        val builtIns = KspContext.builtIns
        defaultValue = when (val ksType = declaration.type.resolve()) {
            builtIns.booleanType -> returnIfNotEquals(optional.booleanValue, false) { true }
            builtIns.charType -> returnIfNotEquals(optional.charValue.code, 0) {
                "'${optional.charValue}'"
            }
            builtIns.byteType -> returnIfNotEquals(optional.byteValue, 0) {
                "${optional.byteValue}.toByte()"
            }
            builtIns.shortType -> returnIfNotEquals(optional.shortValue, 0) {
                "${optional.shortValue}.toShort()"
            }
            builtIns.intType -> returnIfNotEquals(optional.intValue, 0) {
                optional.intValue
            }
            builtIns.longType -> returnIfNotEquals(optional.longValue, 0L) {
                "${optional.longValue}L"
            }
            builtIns.floatType -> returnIfNotEquals(optional.floatValue, 0f) {
                "${optional.floatValue}f"
            }
            builtIns.doubleType -> returnIfNotEquals(optional.doubleValue, 0.0) {
                optional.doubleValue
            }
            builtIns.stringType -> {
                val defaultStringValue = returnIfNotEquals(
                    optional.stringValue.replace(" ", "·"), ""
                ) {
                    """"${optional.stringValue.replace(" ", "·")}""""
                }

                if (Modifier.LATEINIT in declaration.modifiers) {
                    defaultStringValue ?: "\"\""
                } else {
                    defaultStringValue
                }
            }
            else -> {
                "null"
            }
        }
    }

    override fun asKotlinTypeName() = super.asKotlinTypeName().copy(nullable = true)

    override fun compareTo(other: Field): Int {
        return if (other is OptionalField) {
            super.compareTo(other)
        } else {
            //如果与 RequiredField 比较，Optional 永远排在后面
            1
        }
    }

    private fun returnIfNotEquals(value: Any, expect: Any, expression: () -> Any): Any? {
        if (value != expect) {
            return expression()
        }
        return null
    }
}
