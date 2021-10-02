package com.bennyhuo.tieguanyin.compiler.ksp.utils

import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
private fun List<*>.toArray(method: Method, valueProvider: (Any) -> Any): Array<Any?> {
    val array: Array<Any?> = java.lang.reflect.Array.newInstance(
        method.returnType.componentType,
        this.size
    ) as Array<Any?>
    for (r in 0 until this.size) {
        array[r] = this[r]?.let { valueProvider.invoke(it) }
    }
    return array
}

private fun <T> Any.asEnum(returnType: Class<T>): T =
    returnType.getDeclaredMethod("valueOf", String::class.java).invoke(null, this.toString()) as T

private fun Any.asByte(): Byte = if (this is Int) this.toByte() else this as Byte

private fun Any.asShort(): Short = if (this is Int) this.toShort() else this as Short

class TypeNotFoundException(val ksType: KSType, cause: Throwable): Exception(cause)

private fun KSType.asClass() = try {
    logger.warn("asClass = ${this.declaration.qualifiedName!!.asString()}")
    Class.forName(this.declaration.qualifiedName!!.asString())
} catch (e: Exception) {
    throw TypeNotFoundException(this, e)
}

@Suppress("UNCHECKED_CAST")
private fun List<*>.asArray(method: Method) =
    when (method.returnType.componentType.name) {
        "boolean" -> (this as List<Boolean>).toBooleanArray()
        "byte" -> (this as List<Byte>).toByteArray()
        "short" -> (this as List<Short>).toShortArray()
        "char" -> (this as List<Char>).toCharArray()
        "double" -> (this as List<Double>).toDoubleArray()
        "float" -> (this as List<Float>).toFloatArray()
        "int" -> (this as List<Int>).toIntArray()
        "long" -> (this as List<Long>).toLongArray()
        "java.lang.Class" -> (this as List<KSType>).map {
            Class.forName(it.declaration.qualifiedName!!.asString())
        }.toTypedArray()
        "java.lang.String" -> (this as List<String>).toTypedArray()
        else -> { // arrays of enums or annotations
            when {
                method.returnType.componentType.isEnum -> {
                    this.toArray(method) { result -> result.asEnum(method.returnType.componentType) }
                }
                method.returnType.componentType.isAnnotation -> {
                    this.toArray(method) { result ->
                        (result as KSAnnotation).asAnnotation(method.returnType.componentType)
                    }
                }
                else -> throw IllegalStateException("Unable to process type ${method.returnType.componentType.name}")
            }
        }
    }

private fun KSAnnotation.createInvocationHandler(clazz: Class<*>): InvocationHandler {
    val cache = ConcurrentHashMap<Pair<Class<*>, Any>, Any>(arguments.size)
    return InvocationHandler { proxy, method, _ ->
        if (method.name == "toString" && arguments.none { it.name?.asString() == "toString" }) {
            clazz.canonicalName +
                    arguments.map { argument: KSValueArgument ->
                        // handles default values for enums otherwise returns null
                        val methodName = argument.name?.asString()
                        val value = proxy.javaClass.methods.find { m -> m.name == methodName }?.invoke(proxy)
                        "$methodName=$value"
                    }.toList()
        } else {
            logger.warn("invoke: $method, $arguments")

            val argument = try {
                arguments.first { it.name?.asString() == method.name }
            } catch (e: NullPointerException) {
                throw IllegalArgumentException("This is a bug using the default KClass for an annotation", e)
            }
            when (val result = argument.value ?: method.defaultValue) {
                is Proxy -> result
                is List<*> -> {
                    val value = { result.asArray(method) }
                    cache.getOrPut(Pair(method.returnType, result), value)
                }
                else -> {
                    when {
                        method.returnType.isEnum -> {
                            val value = { result.asEnum(method.returnType) }
                            cache.getOrPut(Pair(method.returnType, result), value)
                        }
                        method.returnType.isAnnotation -> {
                            val value = { (result as KSAnnotation).asAnnotation(method.returnType) }
                            cache.getOrPut(Pair(method.returnType, result), value)
                        }
                        method.returnType.isArray -> {
                            val value = {
                                java.lang.reflect.Array.newInstance(method.returnType.componentType, 1).also {
                                    java.lang.reflect.Array.set(it, 0, (result as KSAnnotation).asAnnotation(method.returnType.componentType))
                                }
                            }
                            cache.getOrPut(Pair(method.returnType, result), value)
                        }
                        method.returnType.name == "java.lang.Class" -> {
                            val value = { (result as KSType).asClass() }
                            cache.getOrPut(Pair(method.returnType, result), value)
                        }
                        method.returnType.name == "byte" -> {
                            val value = { result.asByte() }
                            cache.getOrPut(Pair(method.returnType, result), value)
                        }
                        method.returnType.name == "short" -> {
                            val value = { result.asShort() }
                            cache.getOrPut(Pair(method.returnType, result), value)
                        }
                        method.returnType.name == "int" -> {
                            logger.warn("int -- $result")
                            val value = { result.castAs<Int>() }
                            cache.getOrPut(Pair(method.returnType, result), value)
                        }
                        else -> result // original value
                    }
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Annotation> KSAnnotation.toAnnotation(annotationClass: Class<T>): T {
    return Proxy.newProxyInstance(
        annotationClass.classLoader,
        arrayOf(annotationClass),
        createInvocationHandler(annotationClass)
    ) as T
}

private fun KSAnnotation.asAnnotation(
    annotationInterface: Class<*>,
): Any {
    return Proxy.newProxyInstance(
        annotationInterface.classLoader, arrayOf(annotationInterface),
        this.createInvocationHandler(annotationInterface)
    ) as Proxy
}

fun <T : Annotation> KSAnnotated.getAnnotationsByTypeFix(annotationKClass: KClass<T>): Sequence<T> {
    return this.annotations.filter {
        it.shortName.getShortName() == annotationKClass.simpleName && it.annotationType.resolve().declaration
            .qualifiedName?.asString() == annotationKClass.qualifiedName
    }.map { it.toAnnotation(annotationKClass.java) }
}

fun <T : Annotation> KSAnnotated.getFirstAnnotationByType(annotationKClass: KClass<T>): T {
    return getAnnotationsByTypeFix(annotationKClass).first()
}

fun <T : Annotation> KSAnnotated.getFirstAnnotationByTypeOrNull(annotationKClass: KClass<T>): T? {
    return getAnnotationsByTypeFix(annotationKClass).firstOrNull()
}