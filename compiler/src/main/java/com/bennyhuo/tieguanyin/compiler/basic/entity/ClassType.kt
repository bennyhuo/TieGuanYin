package com.bennyhuo.tieguanyin.compiler.basic.entity

import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils
import com.bennyhuo.tieguanyin.compiler.utils.asJavaTypeName
import com.bennyhuo.tieguanyin.compiler.utils.asKotlinTypeName
import com.bennyhuo.tieguanyin.compiler.utils.erasure
import javax.lang.model.type.TypeMirror
import com.squareup.javapoet.TypeName as JavaTypeName
import com.squareup.kotlinpoet.TypeName as KotlinTypeName

class ClassType(private val qName: String, private vararg val parameterizedQNames: String) {
    private val erasuredTypeMirror: TypeMirror by lazy { TypeUtils.getTypeFromClassName(qName).erasure() }
    private val typeMirror: TypeMirror by lazy {
        if(parameterizedQNames.isEmpty()) erasuredTypeMirror
        else TypeUtils.getTypeFromClassName("$qName<${parameterizedQNames.joinToString()}>")
    }

    val java: JavaTypeName by lazy { typeMirror.asJavaTypeName() }
    val kotlin: KotlinTypeName by lazy { typeMirror.asKotlinTypeName() }


}