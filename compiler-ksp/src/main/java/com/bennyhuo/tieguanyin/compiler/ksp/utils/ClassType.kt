package com.bennyhuo.tieguanyin.compiler.ksp.utils

import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.javapoet.TypeName as JavaTypeName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

class ClassType(private val jvmClassName: String, private vararg val typeParameterClassTypes: ClassType) {
    private val typeMirror: KSClassDeclaration by lazy {
        KspContext.resolver.getClassDeclarationByName(jvmClassName) ?: throw ClassNotFoundException(
            jvmClassName
        )
    }

    val java: JavaTypeName by lazy {
        if(typeParameterClassTypes.isNotEmpty()) {
            (typeMirror.toJavaTypeName() as? ClassName)?.let {
                ParameterizedTypeName.get(it, *(Array(typeParameterClassTypes.size) { i -> typeParameterClassTypes[i].java }))
            }?: throw IllegalArgumentException("Only Declared class type should be parameterized.")
        } else {
            typeMirror.toJavaTypeName()
        }
    }
    val kotlin: KotlinTypeName by lazy {
        if (typeParameterClassTypes.isNotEmpty()) {
                logger.warn("ClassType#kotlin: $typeMirror, ${typeParameterClassTypes.joinToString()} -> ${typeMirror.toKotlinTypeName()}")

            (typeMirror.toKotlinTypeName() as? com.squareup.kotlinpoet.ClassName)?.parameterizedBy(*(Array(typeParameterClassTypes.size) { i -> typeParameterClassTypes[i].kotlin }))
                    ?: throw IllegalArgumentException("Only Declared class type should be parameterized.")
        } else {
            typeMirror.toKotlinTypeName()
        }
    }

    fun parameterized(vararg typeParameterClassTypes: ClassType) =
            ClassType(jvmClassName, *typeParameterClassTypes)

    operator fun get(vararg typeParameterClassTypes: ClassType) = ClassType(jvmClassName, *typeParameterClassTypes)

    override fun toString(): String {
        return typeMirror.toString()
    }
}