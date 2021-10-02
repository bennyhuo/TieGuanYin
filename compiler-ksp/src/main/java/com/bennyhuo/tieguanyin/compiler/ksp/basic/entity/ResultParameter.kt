package com.bennyhuo.tieguanyin.compiler.ksp.basic.entity

import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.bennyhuo.tieguanyin.compiler.ksp.utils.TypeNotFoundException
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toJavaTypeName
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKsType
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toTypeName
import java.lang.reflect.UndeclaredThrowableException
import com.squareup.javapoet.TypeName as JavaTypeName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

class ResultParameter(val name: String, val type: KSType) : Comparable<ResultParameter> {

    val javaTypeName: JavaTypeName by lazy { type.toJavaTypeName() }

    val kotlinTypeName: KotlinTypeName by lazy {
        logger.warn("$name -> $type")
        type.toTypeName()
    }

    override fun compareTo(other: ResultParameter) = name.compareTo(other.name)

}

fun ResultEntity.asResultParameter(): ResultParameter {
    return ResultParameter(name, try {
        type.toKsType()
    } catch (e: UndeclaredThrowableException) {
        val cause = e.cause
        if (cause is TypeNotFoundException) {
            cause.ksType
        } else {
            throw e
        }
    })
}