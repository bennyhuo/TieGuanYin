package com.bennyhuo.tieguanyin.compiler.ksp.basic.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class FieldBuilder(private val basicClass: BasicClass) {

    fun build(typeBuilder: TypeSpec.Builder, companionTypeBuilder: TypeSpec.Builder) {
        val builderClassTypeName = ClassName(basicClass.packageName, basicClass.builderClassName)

        val groupedFields = basicClass.fields.groupBy { it is OptionalField }
        val requiredFields = groupedFields[false] ?: emptyList()
        val optionalFields = groupedFields[true] ?: emptyList()

        typeBuilder.addFunction(
            FunSpec.constructorBuilder().addModifiers(KModifier.PRIVATE).build()
        )

        val createBuilderMethodBuilder = FunSpec.builder("builder")
            .returns(builderClassTypeName)
            .addStatement("val builder = %T()", builderClassTypeName)

        val fillIntentMethodBuilder = FunSpec.builder("fillIntent")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("intent", INTENT.kotlin)

        requiredFields.forEach { field ->
            //field
            typeBuilder.addProperty(
                PropertySpec.builder(
                    field.name,
                    field.asTypeName().copy(nullable = true)
                ).mutable(true).initializer("null").build()
            )
            //fillIntent
            fillIntentMethodBuilder.addStatement("intent.putExtra(%S, %L)", field.name, field.name)
            //constructor
            createBuilderMethodBuilder.addParameter(field.name, field.asTypeName())
                .addStatement("builder.%L = %L", field.name, field.name)
        }

        optionalFields.forEach { field ->
            //field
            typeBuilder.addProperty(
                PropertySpec.builder(
                    field.name,
                    field.asTypeName().copy(nullable = true)
                ).mutable(true).initializer("null").build()
            )
            //setter
            typeBuilder.addFunction(
                FunSpec.builder(field.name)
                    .addParameter(field.name, field.asTypeName())
                    .addStatement("this.${field.name} = ${field.name}")
                    .addStatement("return this")
                    .returns(builderClassTypeName)
                    .build()
            )
            //fillIntent
            fillIntentMethodBuilder.addStatement("intent.putExtra(%S, %L)", field.name, field.name)
        }

        companionTypeBuilder.addFunction(
            createBuilderMethodBuilder
                .addStatement("return builder")
                .addAnnotation(JvmStatic::class)
                .build()
        )

        typeBuilder.addFunction(fillIntentMethodBuilder.build())
    }

}