package com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT_ACTIVITY
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.STRING
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW_GROUP
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.Op.ADD
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.Op.REPLACE
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec


/**
 * Created by benny on 1/31/18.
 */

abstract class StartFragmentKFunctionBuilder(private val fragmentClass: FragmentClass) {

    abstract val prefix: String
    abstract val op: Op

    val name: String by lazy {
        "${prefix}${fragmentClass.simpleName}"
    }

    open fun build(fileBuilder: FileSpec.Builder) {
        val returnType = fragmentClass.declaration.toKotlinTypeName().copy(nullable = true)
        val funBuilderOfActivity = FunSpec.builder(name)
                .receiver(FRAGMENT_ACTIVITY.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(returnType)
                .addParameter("containerId", INT)

        val groupedFields = fragmentClass.fields.groupBy { it is OptionalField }
        val requiredFields = groupedFields[false] ?: emptyList()
        val optionalFields = groupedFields[true] ?: emptyList()

        fragmentClass.fields.forEach { requiredField ->
            funBuilderOfActivity.addParameter(
                ParameterSpec.builder(requiredField.name, requiredField.asKotlinTypeName())
                    .also {
                        if (requiredField is OptionalField) it.defaultValue("null")
                    }.build()
            )
        }

        funBuilderOfActivity.addStatement(
            "val builder = %L.builder(%L)",
            fragmentClass.builderClassName,
            requiredFields.joinToString { it.name }
        )

        optionalFields.forEach {
            funBuilderOfActivity.addStatement("builder.%N(%N)", it.name, it.name)
        }

        funBuilderOfActivity.addParameter(
            ParameterSpec.builder("tag", STRING.kotlin.copy(nullable = true))
                .defaultValue("null")
                .build()
        )

        funBuilderOfActivity.addStatement("return builder.%L(this, containerId, tag)", prefix)

        val parameterSpecs = funBuilderOfActivity.parameters.let { it.subList(1, it.size) }
        val parameterLiteral = parameterSpecs.joinToString { it.name }

        fileBuilder.addFunction(funBuilderOfActivity.build())

        fileBuilder.addFunction(FunSpec.builder(name)
                .receiver(VIEW_GROUP.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(returnType)
                .addParameters(parameterSpecs)
                .addStatement("return (context as? %T)?.%L(id %L)", FRAGMENT_ACTIVITY.kotlin, name, if (parameterLiteral.isBlank()) parameterLiteral else ", $parameterLiteral").build())

        fileBuilder.addFunction(FunSpec.builder(name)
                .receiver(FRAGMENT.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(returnType)
                .addParameters(parameterSpecs)
                .addStatement("return (view?.parent as? %T)?.%L(%L)", VIEW_GROUP.kotlin, name, parameterLiteral).build())

        when(op){
            Op.ADD -> {
                fileBuilder.addFunction(FunSpec.builder(name)
                        .receiver(FRAGMENT_ACTIVITY.kotlin)
                        .addModifiers(KModifier.PUBLIC)
                        .returns(returnType)
                        .addParameter("tag", STRING.kotlin)
                        .addParameters(parameterSpecs.subList(0, parameterSpecs.size - 1))
                        .addParameter(ParameterSpec.builder("containerId", INT).defaultValue("0").build())
                        .addStatement("return %L(containerId, %L)", name, parameterLiteral).build())
            }
            Op.REPLACE -> {

            }
        }
    }
}

class ReplaceKFunctionBuilder(fragmentClass: FragmentClass): StartFragmentKFunctionBuilder(fragmentClass) {
    override val prefix: String = "replace"
    override val op: Op = REPLACE
}

class AddKFunctionBuilder(fragmentClass: FragmentClass): StartFragmentKFunctionBuilder(fragmentClass) {
    override val prefix: String = "add"
    override val op: Op = ADD
}
