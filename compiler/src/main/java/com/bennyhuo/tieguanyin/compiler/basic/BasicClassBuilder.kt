package com.bennyhuo.tieguanyin.compiler.basic

import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.compiler.basic.types.GENERATED_ANNOTATION
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import java.io.IOException
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.tools.StandardLocation

abstract class BasicClassBuilder<T: BasicClass<T>>(private val basicClass: T) {

    private fun writeJavaToFile(filer: Filer, typeSpec: TypeSpec) {
        val file = JavaFile.builder(basicClass.packageName, typeSpec).build()
        file.writeTo(filer)
    }

    private fun writeKotlinToFile(filer: Filer, fileSpec: FileSpec) {
        val fileObject = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            basicClass.packageName,
            fileSpec.name + ".kt",
            basicClass.typeElement
        )
        fileObject.openWriter().use {
            fileSpec.writeTo(it)
        }
    }

    fun build(filer: Filer) {
        if (basicClass.isAbstract) return

        val typeBuilder = TypeSpec.classBuilder(basicClass.builderClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(GENERATED_ANNOTATION.java as ClassName)
            .addOriginatingElement(basicClass.typeElement)

        buildCommon(typeBuilder)

        when (basicClass.generateMode) {
            GenerateMode.JavaOnly -> buildJavaBuilders(typeBuilder)
            GenerateMode.KotlinOnly -> {
                val fileSpecBuilder =
                    FileSpec.builder(basicClass.packageName, basicClass.builderClassName)
                        .addAnnotation(GENERATED_ANNOTATION.kotlin as com.squareup.kotlinpoet.ClassName)
                buildKotlinBuilders(fileSpecBuilder)
                writeKotlinToFile(filer, fileSpecBuilder.build())
            }
            GenerateMode.Both -> {
                buildJavaBuilders(typeBuilder)

                val fileSpecBuilder =
                    FileSpec.builder(basicClass.packageName, basicClass.builderClassName)
                buildKotlinBuilders(fileSpecBuilder)
                writeKotlinToFile(filer, fileSpecBuilder.build())
            }
            GenerateMode.Auto -> {
                //Won't happen.
            }
        }

        writeJavaToFile(filer, typeBuilder.build())
    }

    abstract fun buildCommon(typeBuilder: TypeSpec.Builder)
    abstract fun buildKotlinBuilders(fileBuilder: FileSpec.Builder)
    abstract fun buildJavaBuilders(typeBuilder: TypeSpec.Builder)

}