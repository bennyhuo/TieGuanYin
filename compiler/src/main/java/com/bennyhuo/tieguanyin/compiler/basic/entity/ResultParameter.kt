package com.bennyhuo.tieguanyin.compiler.basic.entity

import com.bennyhuo.aptutils.logger.Logger
import com.bennyhuo.aptutils.types.asJavaTypeName
import com.bennyhuo.aptutils.types.asKotlinTypeName
import com.bennyhuo.aptutils.types.asTypeMirror
import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.basic.types.*
import com.sun.tools.javac.code.Type
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import com.squareup.javapoet.TypeName as JavaTypeName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

class ResultParameter(val name: String, val type: TypeMirror): Comparable<ResultParameter>{

    val javaTypeName: JavaTypeName  by lazy { type.asJavaTypeName() }

    val kotlinTypeName: KotlinTypeName  by lazy { type.asKotlinTypeName() }

    val isInternalType: Boolean = SupportedTypes.isInternalType(type as Type)

    val isAnnotatedType: Boolean = DataType.isAnnotatedType(type as Type)

    override fun compareTo(other: ResultParameter) = name.compareTo(other.name)

    fun javaTemplateFromBundle(bundleName: String): Pair<String, Array<out Any>>{
        return if(isInternalType) {
            "\$T.<\$T>get($bundleName,\$S)" to arrayOf(RUNTIME_UTILS.java, javaTypeName.box(), name)
        } else if(isAnnotatedType){
            "\$T.<\$T, \$T>findProperConverter(\$T.class).convertTo(\$T.<\$T>get($bundleName, \$S))" to
                    arrayOf(TIEGUANYIN.java, javaTypeName.box(), BUNDLE.java, javaTypeName.box(), RUNTIME_UTILS.java, BUNDLE.java, name)
        } else {
            throw UnsupportedOperationException("Unsupported type:  $type")
        }
    }

    fun javaTemplateToBundle(): Pair<String, Array<out Any?>>{
        return if(isInternalType){
            name to emptyArray()
        } else if(isAnnotatedType){
            "\$T.<\$T, \$T>findProperConverter(\$T.class).convertFrom($name)" to arrayOf(TIEGUANYIN.java, javaTypeName, BUNDLE.java, javaTypeName)
        } else {
            throw UnsupportedOperationException("Unsupported type:  $type")
        }
    }

    fun kotlinTemplateFromBundle(bundleName: String): Pair<String, Array<out Any>>{
        return if(isInternalType) {
            "%T.get<%T>($bundleName, %S)" to arrayOf(RUNTIME_UTILS.kotlin, kotlinTypeName, name)
        } else if(isAnnotatedType){
            "%T.findProperConverter<%T, %T>(%T::class.java).convertTo(%T.get<%T>($bundleName, %S))" to
                    arrayOf(TIEGUANYIN.kotlin, kotlinTypeName, BUNDLE.kotlin, kotlinTypeName, RUNTIME_UTILS.kotlin, BUNDLE.kotlin, name)
        } else {
            throw UnsupportedOperationException("Unsupported type:  $type")
        }
    }

    fun kotlinTemplateToBundle(): Pair<String, Array<out Any>>{
        return if(isInternalType){
            name to emptyArray()
        } else if(isAnnotatedType){
            "%T.findProperConverter<%T, %T>(%T::class.java).convertFrom($name)" to arrayOf(TIEGUANYIN.kotlin, kotlinTypeName, BUNDLE.kotlin, kotlinTypeName)
        } else {
            throw UnsupportedOperationException("Unsupported type:  $type")
        }
    }
}

fun ResultEntity.asResultParameter() = ResultParameter(name, resultType)

val ResultEntity.resultType: TypeMirror
    get() = try {
        type.asTypeMirror()
    } catch (e: MirroredTypeException) {
        e.typeMirror.also {
            Logger.warn("Result type: ${it.javaClass}")
        }
    }