package com.bennyhuo.tieguanyin.compiler.ksp.basic.entity

import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.ksp.utils.KsTypeNotPresentException
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKsType
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

class ResultParameter(val name: String, val type: KSType) : Comparable<ResultParameter> {

    val kotlinTypeName: KotlinTypeName by lazy {
        type.toTypeName()
    }

    override fun compareTo(other: ResultParameter) = name.compareTo(other.name)

}

fun ResultEntity.asResultParameter(): ResultParameter {
    return ResultParameter(name, try {
        type.toKsType()
    } catch (e: Exception) {
        if (e is KsTypeNotPresentException) {
            e.ksType
        } else {
            throw e
        }
    })
}