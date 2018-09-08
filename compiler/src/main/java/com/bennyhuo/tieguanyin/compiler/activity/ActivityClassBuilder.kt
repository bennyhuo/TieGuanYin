package com.bennyhuo.tieguanyin.compiler.activity

import com.bennyhuo.tieguanyin.annotations.GenerateMode
import com.bennyhuo.tieguanyin.annotations.GenerateMode.Auto
import com.bennyhuo.tieguanyin.compiler.activity.methods.*
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.tools.StandardLocation

class ActivityClassBuilder(private val activityClass: ActivityClass) {

    private fun writeJavaToFile(filer: Filer, typeSpec: TypeSpec) {
        JavaFile.builder(activityClass.packageName, typeSpec)
                .build()
                .writeTo(filer)
    }

    private fun writeKotlinToFile(filer: Filer, fileSpec: FileSpec) {
        filer.createResource(StandardLocation.SOURCE_OUTPUT, activityClass.packageName, fileSpec.name + ".kt")
                .openWriter()
                .also(fileSpec::writeTo)
                .close()
    }

    fun build(filer: Filer) {
        if (activityClass.type.modifiers.contains(Modifier.ABSTRACT)) return
        val typeBuilder = TypeSpec.classBuilder(activityClass.simpleName + POSIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        ConstantBuilder(activityClass).build(typeBuilder)
        InjectMethodBuilder(activityClass).build(typeBuilder)
        SaveStateMethodBuilder(activityClass).build(typeBuilder)

        activityClass.activityResultClass?.buildOnActivityResultListenerInterface()?.let(typeBuilder::addType)

        when (activityClass.generateMode) {
            GenerateMode.JavaOnly -> {
                StartMethodBuilder(activityClass).build(typeBuilder)
                FinishMethodBuilder(activityClass).build(typeBuilder)

                activityClass.activityResultClass?.buildFinishWithResultMethod()?.let(typeBuilder::addMethod)
            }
            GenerateMode.Both -> {
                StartMethodBuilder(activityClass).build(typeBuilder)
                FinishMethodBuilder(activityClass).build(typeBuilder)

                activityClass.activityResultClass?.buildFinishWithResultMethod()?.let(typeBuilder::addMethod)

                //region kotlin
                val fileSpecBuilder = FileSpec.builder(activityClass.packageName, activityClass.simpleName + POSIX)
                StartKotlinFunctionBuilder(activityClass, EXT_FUN_NAME_PREFIX + activityClass.simpleName).build(fileSpecBuilder);
                FinishKotlinFunctionBuilder(activityClass).build(fileSpecBuilder)

                activityClass.activityResultClass?.buildFinishWithResultKt()?.let(fileSpecBuilder::addFunction)

                writeKotlinToFile(filer, fileSpecBuilder.build())
            }
            GenerateMode.KotlinOnly -> {
                val fileSpecBuilder = FileSpec.builder(activityClass.packageName, activityClass.simpleName + POSIX)
                StartKotlinFunctionBuilder(activityClass, EXT_FUN_NAME_PREFIX + activityClass.simpleName).build(fileSpecBuilder);
                FinishKotlinFunctionBuilder(activityClass).build(fileSpecBuilder)
                activityClass.activityResultClass?.buildFinishWithResultKt()?.let(fileSpecBuilder::addFunction)
                writeKotlinToFile(filer, fileSpecBuilder.build())
            }
            Auto -> {
                //Won't happen.
            }
        }//endregion

        writeJavaToFile(filer, typeBuilder.build())
    }

    companion object {
        const val METHOD_NAME = "start"
        const val METHOD_NAME_NO_OPTIONAL = METHOD_NAME + "WithoutOptional"
        const val METHOD_NAME_FOR_OPTIONAL = METHOD_NAME + "WithOptional"
        const val EXT_FUN_NAME_PREFIX = METHOD_NAME
        const val POSIX = "Builder"
        const val CONSTS_RESULT_PREFIX = "RESULT_"
    }
}