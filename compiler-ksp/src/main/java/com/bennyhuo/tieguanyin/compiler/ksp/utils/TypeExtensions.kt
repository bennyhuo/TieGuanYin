package com.bennyhuo.tieguanyin.compiler.ksp.utils

import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.javapoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.reflect.KClass

inline fun <reified T> Any?.safeCastAs() = this as? T
inline fun <reified T> Any.castAs() = this as T

fun KClass<*>.toKsType(): KSType {
    logger.warn(this.toString())
    return KspContext.resolver
        .getClassDeclarationByName(qualifiedName!!)!!
        .asType(emptyList())
}

val KSType.isNonNull: Boolean
    get() = nullability == Nullability.NOT_NULL

val KSType.isNullable: Boolean
    get() = nullability == Nullability.NULLABLE || nullability == Nullability.PLATFORM

fun KSType.isSubTypeOf(typeName: String): Boolean {
    return KspContext.resolver.getClassDeclarationByName(typeName)
        ?.asStarProjectedType()
        ?.isAssignableFrom(this) == true
}

fun KSClassDeclaration.asType() = asStarProjectedType()

fun KSClassDeclaration.superType(): KSClassDeclaration? {
    return superTypes.map { it.resolve().declaration }
        .filterIsInstance<KSClassDeclaration>()
        .filter { it.classKind == ClassKind.CLASS }
        .firstOrNull()
}

fun KSType.toJavaTypeName() = declaration.castAs<KSClassDeclaration>().toJavaTypeName()

fun KSClassDeclaration.toJavaTypeName(): ClassName {
    logger.warn("toJava: $this ${qualifiedName?.asString()} ----->")
    val javaTypeName = qualifiedName!!.let {
        if (it.asString().startsWith("kotlin")) {
            KspContext.resolver.mapKotlinNameToJava(it) ?: it
        } else it
    }
    logger.warn("toJava: ${qualifiedName?.asString()} -> ${javaTypeName.asString()}")
    return ClassName.bestGuess(javaTypeName.asString())
}

fun KSClassDeclaration.toKotlinTypeName() = asStarProjectedType().toTypeName()