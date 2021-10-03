package com.bennyhuo.tieguanyin.compiler.ksp.utils

import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.javapoet.ClassName as JavaClassName
import com.squareup.javapoet.TypeName as JavaTypeName
import com.squareup.kotlinpoet.ClassName as KotlinClassName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

class ClassType(private val jvmClassName: String, private vararg val typeParameterClassTypes: ClassType) {

    val kotlin: KotlinTypeName by lazy {
        val className = KotlinClassName.bestGuess(jvmClassName)
        if (typeParameterClassTypes.isNotEmpty()) {
            className.parameterizedBy(*(Array(typeParameterClassTypes.size) { i -> typeParameterClassTypes[i].kotlin }))
        } else {
            className
        }
    }

    fun parameterized(vararg typeParameterClassTypes: ClassType) =
            ClassType(jvmClassName, *typeParameterClassTypes)

    operator fun get(vararg typeParameterClassTypes: ClassType) = ClassType(jvmClassName, *typeParameterClassTypes)

    override fun toString(): String {
        return "${jvmClassName}[${typeParameterClassTypes.joinToString()}]"
    }
}