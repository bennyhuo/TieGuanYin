package com.bennyhuo.tieguanyin.compiler.ksp.basic

import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.GENERATED_ANNOTATION
import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

abstract class BasicClassBuilder(private val basicClass: BasicClass) {

    private fun writeKotlinToFile(fileSpec: FileSpec) {
        fileSpec.writeTo(KspContext.environment.codeGenerator, false, listOf(basicClass.typeElement.containingFile!!))
    }

    fun build() {
        if (basicClass.isAbstract) return

        val fileSpecBuilder = FileSpec.builder(basicClass.packageName, basicClass.builderClassName)
            .addAnnotation(GENERATED_ANNOTATION.kotlin as ClassName)

        val typeBuilder = TypeSpec.classBuilder(basicClass.builderClassName)
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
            .addAnnotation(GENERATED_ANNOTATION.kotlin as ClassName)

        buildCommon(typeBuilder)
        buildJavaBuilders(typeBuilder)

        buildKotlinBuilders(fileSpecBuilder)

        fileSpecBuilder.addType(typeBuilder.build())
        writeKotlinToFile(fileSpecBuilder.build())
    }

    abstract fun buildCommon(typeBuilder: TypeSpec.Builder)
    abstract fun buildKotlinBuilders(fileBuilder: FileSpec.Builder)
    abstract fun buildJavaBuilders(typeBuilder: TypeSpec.Builder)

}