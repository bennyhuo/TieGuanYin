package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.entity.KotlinOnResultListener
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_BUILDER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.CONTEXT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

/**
 * Created by benny on 1/31/18.
 */

class StartKFunctionBuilder(private val activityClass: ActivityClass) {

    private val name = ActivityClassBuilder.METHOD_NAME + activityClass.simpleName

    fun build(fileBuilder: FileSpec.Builder) {
        val functionBuilderOfContext = FunSpec.builder(name)
            .receiver(CONTEXT.kotlin)
            .addModifiers(KModifier.PUBLIC)

        val groupedFields = activityClass.fields.groupBy { it is OptionalField }
        val requiredFields = groupedFields[false] ?: emptyList()
        val optionalFields = groupedFields[true] ?: emptyList()

        activityClass.fields.forEach { field ->
            functionBuilderOfContext.addParameter(
                ParameterSpec.builder(field.name, field.asKotlinTypeName())
                    .addKdoc(field.docString)
                    .also {
                        if (field is OptionalField) it.defaultValue("null")
                    }.build()
            )
        }

        functionBuilderOfContext.addStatement(
            "val builder = %L.builder(%L)",
            activityClass.builderClassName,
            requiredFields.joinToString { it.name }
        )

        optionalFields.forEach {
            functionBuilderOfContext.addStatement("builder.%N(%N)", it.name, it.name)
        }

        if (activityClass.hasResult) {
            val kotlinOnResultListener = KotlinOnResultListener(activityClass)

            functionBuilderOfContext
                .addStatement("builder.start(this, %N)", kotlinOnResultListener.name)
                .addParameter(
                    ParameterSpec.builder(
                        kotlinOnResultListener.name,
                        kotlinOnResultListener.typeName
                    ).defaultValue("null").build()
                )
        } else {
            functionBuilderOfContext.addStatement("builder.start(this)")
        }

        val parameterLiteral = functionBuilderOfContext.parameters.joinToString(",") { it.name }

        // Context.start(...)
        fileBuilder.addFunction(functionBuilderOfContext.build())

        // View.start(...)
        fileBuilder.addFunction(
            FunSpec.builder(name)
                .receiver(VIEW.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .addParameters(functionBuilderOfContext.parameters)
                .addStatement(
                    "%T.INSTANCE.findProperActivity(this)?.%L(%L)",
                    ACTIVITY_BUILDER.kotlin,
                    name,
                    parameterLiteral
                )
                .build()
        )

        // Fragment.start(...)
        fileBuilder.addFunction(
            FunSpec.builder(name)
                .receiver(FRAGMENT.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .addParameters(functionBuilderOfContext.parameters)
                .addStatement("view?.%L(%L)", name, parameterLiteral)
                .build()
        )
    }
}
