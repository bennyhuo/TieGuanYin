package com.bennyhuo.tieguanyin.compiler.utils

import com.squareup.javapoet.TypeName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

/**
 * Created by benny on 2/3/18.
 */
object TypeUtils {
    lateinit var types: Types
    lateinit var elements: Elements

    private fun doubleErasure(elementType: TypeMirror): String {
        var name = types.erasure(elementType).toString()
        val typeParamStart = name.indexOf('<')
        if (typeParamStart != -1) {
            name = name.substring(0, typeParamStart)
        }
        return name
    }

    fun simpleName(elementType: TypeMirror): String {
        val name = doubleErasure(elementType)
        return name.substring(name.lastIndexOf(".") + 1)
    }

    fun getPackageName(type: TypeElement): String {
        return if (type.enclosingElement.kind == ElementKind.PACKAGE) {
            type.enclosingElement.asType().toString()
        } else {
            throw IllegalArgumentException(type.enclosingElement.toString())
        }
    }

    fun getTypeFromClassName(className: String) = elements.getTypeElement(className).asType()

}

fun TypeMirror.erasure() = TypeUtils.types.erasure(this)

fun TypeMirror.isSubTypeOf(className: String): Boolean {
    return TypeUtils.types.isSubtype(this, TypeUtils.getTypeFromClassName(className))
}

fun Class<*>.asTypeMirror(): TypeMirror {
    return TypeUtils.elements.getTypeElement(canonicalName).asType()
}

fun KClass<*>.asTypeMirror(): TypeMirror {
    return TypeUtils.elements.getTypeElement(qualifiedName).asType()
}

fun TypeMirror.asJavaTypeName() = TypeName.get(this)

fun TypeMirror.asKotlinTypeName(): KotlinTypeName {
    when (kind) {
        TypeKind.BOOLEAN -> return BOOLEAN
        TypeKind.BYTE -> return BYTE
        TypeKind.SHORT -> return SHORT
        TypeKind.INT -> return INT
        TypeKind.LONG -> return LONG
        TypeKind.CHAR -> return CHAR
        TypeKind.FLOAT -> return FLOAT
        TypeKind.DOUBLE -> return DOUBLE
        TypeKind.DECLARED -> if (toString() == "java.lang.String") {
            return STRING
        }
        TypeKind.ARRAY -> {
            val arrayType = this as ArrayType
            when (arrayType.componentType.kind) {
                TypeKind.BOOLEAN -> return BOOLEAN_ARRAY
                TypeKind.BYTE -> return BYTE_ARRAY
                TypeKind.SHORT -> return SHORT_ARRAY
                TypeKind.INT -> return INT_ARRAY
                TypeKind.LONG -> return LONG_ARRAY
                TypeKind.CHAR -> return CHAR_ARRAY
                TypeKind.FLOAT -> return FLOAT_ARRAY
                TypeKind.DOUBLE -> return DOUBLE_ARRAY
                TypeKind.DECLARED -> if (toString() == "java.lang.String[]") {
                    return STRING_ARRAY
                }
            }
        }
    }
    return asTypeName()
}

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