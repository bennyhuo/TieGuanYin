package com.bennyhuo.tieguanyin.compiler.ksp.basic.entity

import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

/**
 * Created by benny on 1/31/18.
 */

class OptionalField(declaration: KSPropertyDeclaration, optional: Optional) : Field(declaration) {

    var defaultValue: Any
        private set

    override val prefix = "OPTIONAL_"

    init {
        val builtIns = KspContext.builtIns
        defaultValue = when (declaration.type.resolve()) {
            builtIns.booleanType -> optional.booleanValue
            builtIns.charType -> "'${optional.charValue}'"
            builtIns.byteType -> "${optional.byteValue}.toByte()"
            builtIns.shortType -> "${optional.shortValue}.toShort()"
            builtIns.intType -> optional.intValue
            builtIns.longType -> "${optional.longValue}L"
            builtIns.floatType -> "${optional.floatValue}f"
            builtIns.doubleType -> optional.doubleValue
            builtIns.stringType -> """"${optional.stringValue}""""
            else -> "null"
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
}
