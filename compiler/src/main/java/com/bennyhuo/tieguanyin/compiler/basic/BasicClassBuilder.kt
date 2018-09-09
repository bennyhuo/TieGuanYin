package com.bennyhuo.tieguanyin.compiler.basic

import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClassBuilder
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import java.io.IOException
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.tools.StandardLocation

abstract class BasicClassBuilder(private val basicClass: BasicClass){

    private fun writeJavaToFile(filer: Filer, typeSpec: TypeSpec) {
        try {
            val file = JavaFile.builder(basicClass.packageName, typeSpec).build()
            file.writeTo(filer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeKotlinToFile(filer: Filer, fileSpec: FileSpec) {
        try {
            val fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, basicClass.packageName, fileSpec.name + ".kt")
            val writer = fileObject.openWriter()
            fileSpec.writeTo(writer)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun build(filer: Filer){
        if (basicClass.isAbstract) return

        val typeBuilder = TypeSpec.classBuilder(basicClass.simpleName + FragmentClassBuilder.POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        buildCommon(typeBuilder)

        when(basicClass.generateMode){
            GenerateMode.JavaOnly -> buildJavaBuilders(typeBuilder)
            GenerateMode.KotlinOnly -> {
                val fileSpecBuilder = FileSpec.builder(basicClass.packageName, basicClass.simpleName + FragmentClassBuilder.POSIX)
                buildKotlinBuilders(fileSpecBuilder)
                writeKotlinToFile(filer, fileSpecBuilder.build())
            }
            GenerateMode.Both -> {
                buildJavaBuilders(typeBuilder)

                val fileSpecBuilder = FileSpec.builder(basicClass.packageName, basicClass.simpleName + FragmentClassBuilder.POSIX)
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