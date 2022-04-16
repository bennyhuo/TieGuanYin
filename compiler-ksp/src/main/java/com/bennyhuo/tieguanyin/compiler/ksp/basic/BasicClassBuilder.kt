package com.bennyhuo.tieguanyin.compiler.ksp.basic

import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.GENERATED_ANNOTATION
import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo

abstract class BasicClassBuilder(private val basicClass: BasicClass) {

    private fun writeKotlinToFile(fileSpec: FileSpec) {
        fileSpec.writeTo(KspContext.environment.codeGenerator, false, listOf(basicClass.declaration.containingFile!!))
    }

    fun build() {
        try {
            if (basicClass.isAbstract) return

            val fileSpecBuilder = FileSpec.builder(basicClass.packageName, basicClass.builderClassName)
                .addAnnotation(GENERATED_ANNOTATION.kotlin as ClassName)

            val typeBuilder = TypeSpec.classBuilder(basicClass.builderClassName)
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                .addAnnotation(GENERATED_ANNOTATION.kotlin as ClassName)

            buildBuilderClass(typeBuilder)
            buildKotlinExtensions(fileSpecBuilder)

            fileSpecBuilder.addType(typeBuilder.build())
            writeKotlinToFile(fileSpecBuilder.build())
        } catch (e: Exception) {
            logger.error(e.toString(), basicClass.declaration)
            throw e
        }
    }

    abstract fun buildBuilderClass(typeBuilder: TypeSpec.Builder)
    abstract fun buildKotlinExtensions(fileBuilder: FileSpec.Builder)

}