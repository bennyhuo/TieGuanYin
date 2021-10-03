package com.bennyhuo.tieguanyin.compiler.ksp.basic

import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.GENERATED_ANNOTATION
import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.lang.model.element.Modifier

abstract class BasicClassBuilder(private val basicClass: BasicClass) {

    private fun writeJavaToFile(typeSpec: TypeSpec) {
        val javaFile = JavaFile.builder(basicClass.packageName, typeSpec).build()
        val file = KspContext.environment.codeGenerator.createNewFile(
            Dependencies(false), basicClass.packageName, typeSpec.name, "java"
        )
        OutputStreamWriter(file, StandardCharsets.UTF_8).use(javaFile::writeTo)
    }

    private fun writeKotlinToFile(fileSpec: FileSpec) {
        fileSpec.writeTo(KspContext.environment.codeGenerator, false)
    }

    fun build() {
        if (basicClass.isAbstract) return

        val typeBuilder = TypeSpec.classBuilder(basicClass.builderClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(GENERATED_ANNOTATION.java as ClassName)

        buildCommon(typeBuilder)

        val fileSpecBuilder = FileSpec.builder(basicClass.packageName, basicClass.builderClassName)
            .addAnnotation(GENERATED_ANNOTATION.kotlin as com.squareup.kotlinpoet.ClassName)
        buildKotlinBuilders(fileSpecBuilder)
        writeKotlinToFile(fileSpecBuilder.build())

        writeJavaToFile(typeBuilder.build())
    }

    abstract fun buildCommon(typeBuilder: TypeSpec.Builder)
    abstract fun buildKotlinBuilders(fileBuilder: FileSpec.Builder)
    abstract fun buildJavaBuilders(typeBuilder: TypeSpec.Builder)

}