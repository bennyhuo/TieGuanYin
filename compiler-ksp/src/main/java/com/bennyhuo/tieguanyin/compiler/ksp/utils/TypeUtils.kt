package com.bennyhuo.tieguanyin.compiler.ksp.utils

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Created by benny on 2/3/18.
 */

private val STRING: ClassName = ClassName("kotlin", "String")
private val STRING_ARRAY = ClassName("kotlin", "Array").parameterizedBy(STRING)
private val LONG_ARRAY: ClassName = ClassName("kotlin", "LongArray")
private val INT_ARRAY: ClassName = ClassName("kotlin", "IntArray")
private val SHORT_ARRAY: ClassName = ClassName("kotlin", "ShortArray")
private val BYTE_ARRAY: ClassName = ClassName("kotlin", "ByteArray")
private val CHAR_ARRAY: ClassName = ClassName("kotlin", "CharArray")
private val BOOLEAN_ARRAY: ClassName = ClassName("kotlin", "BooleanArray")
private val FLOAT_ARRAY: ClassName = ClassName("kotlin", "FloatArray")
private val DOUBLE_ARRAY: ClassName = ClassName("kotlin", "DoubleArray")
//endregion