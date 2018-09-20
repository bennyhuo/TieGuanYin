package com.bennyhuo.aptutils.types

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.type.TypeMirror
import com.squareup.javapoet.TypeName as JavaTypeName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

class ClassType(private val jvmClassName: String, private vararg val typeParameterClassTypes: ClassType) {
    private val typeMirror: TypeMirror by lazy {
        TypeUtils.getTypeFromClassName(jvmClassName).erasure()
    }

    val java: JavaTypeName by lazy {
        if(typeParameterClassTypes.isNotEmpty()) {
            (typeMirror.asJavaTypeName() as? ClassName)?.let {
                ParameterizedTypeName.get(it, *(Array(typeParameterClassTypes.size) { i -> typeParameterClassTypes[i].java }))
            }?: throw IllegalArgumentException("Only Declared class type should be parameterized.")
        } else {
            typeMirror.asJavaTypeName()
        }
    }
    val kotlin: KotlinTypeName by lazy {
        if (typeParameterClassTypes.isNotEmpty()) {
            (typeMirror.asKotlinTypeName() as? com.squareup.kotlinpoet.ClassName)?.parameterizedBy(*(Array(typeParameterClassTypes.size) { i -> typeParameterClassTypes[i].kotlin }))
                    ?: throw IllegalArgumentException("Only Declared class type should be parameterized.")
        } else {
            typeMirror.asKotlinTypeName()
        }
    }

    fun parameterized(vararg typeParameterClassTypes: ClassType) =
            ClassType(jvmClassName, *typeParameterClassTypes)

    operator fun get(vararg typeParameterClassTypes: ClassType) = ClassType(jvmClassName, *typeParameterClassTypes)

    override fun toString(): String {
        return typeMirror.toString()
    }
}