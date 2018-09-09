package com.bennyhuo.tieguanyin.compiler.utils

import com.squareup.javapoet.TypeName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass

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

fun TypeMirror.asKotlinTypeName() = KotlinTypes.toKotlinType(this)

