package com.bennyhuo.tieguanyin.compiler.basic.types

import com.sun.tools.javac.code.Type
import javax.lang.model.element.TypeElement

class DataType(val typeElement: TypeElement) {
    companion object {
        val dataTypes = HashMap<Type, DataType>()

        fun isAnnotatedType(type: Type): Boolean {
            return dataTypes.containsKey(type)
        }
    }
}