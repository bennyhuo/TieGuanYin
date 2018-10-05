package com.bennyhuo.tieguanyin.compiler.fragment.builder

import com.bennyhuo.aptutils.types.asKotlinTypeName
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.basic.types.*
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.squareup.kotlinpoet.*

/**
 * Created by benny on 1/31/18.
 */

class ReplaceKFunctionBuilder(private val fragmentClass: FragmentClass) {

    fun build(fileBuilder: FileSpec.Builder) {
        val name = "replace" + fragmentClass.simpleName
        val returnType = fragmentClass.typeElement.asType().asKotlinTypeName().asNullable()
        val funBuilderOfContext = FunSpec.builder(name)
                .receiver(SUPPORT_ACTIVITY.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(returnType)
                .addParameter("containerId", INT)
                .addStatement("%T.INSTANCE.init(this)", ACTIVITY_BUILDER.kotlin)
                .addStatement("val intent = %T()", INTENT.kotlin)

        for (field in fragmentClass.fields) {
            val name = field.name
            val className = field.asKotlinTypeName()
            if (field is OptionalField) {
                funBuilderOfContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build())
            } else {
                funBuilderOfContext.addParameter(name, className)
            }
            funBuilderOfContext.addStatement("intent.putExtra(%S, %L)", name, name)
        }

        funBuilderOfContext.addParameter(ParameterSpec.builder("tag", STRING.kotlin.asNullable()).defaultValue("null").build())

        val sharedElements = fragmentClass.sharedElements
        if (sharedElements.isEmpty()) {
            funBuilderOfContext.addStatement("return %T.showFragment(this, true, containerId, tag, intent.getExtras(), %T::class.java, null)", FRAGMENT_BUILDER.kotlin, fragmentClass.typeElement)
        } else {
            funBuilderOfContext.addStatement("val sharedElements = %T()", ARRAY_LIST[SUPPORT_PAIR[STRING, STRING]].kotlin)
                    .addStatement("val container: %T = findViewById(containerId)", VIEW.kotlin)
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceName != null) {
                    funBuilderOfContext.addStatement("sharedElements.add(Pair(%S, %S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    funBuilderOfContext.addStatement("sharedElements.add(Pair(%T.getTransitionName(container.findViewById(%L)), %S))", VIEW_COMPAT.kotlin, sharedElement.sourceId, sharedElement.targetName)
                }
            }
            funBuilderOfContext.addStatement("return %T.showFragment(this, true, containerId, tag, intent.getExtras(), %T::class.java, sharedElements)", FRAGMENT_BUILDER.kotlin, fragmentClass.typeElement)
        }


        val parameterSpecs = funBuilderOfContext.parameters.let { it.subList(1, it.size) }
        val parameterLiteral = parameterSpecs.joinToString { it.name }

        fileBuilder.addFunction(funBuilderOfContext.build())

        fileBuilder.addFunction(FunSpec.builder(name)
                .receiver(VIEW_GROUP.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(returnType)
                .addParameters(parameterSpecs)
                .addStatement("return (context as? %T)?.%L(id %L)", SUPPORT_ACTIVITY.kotlin, name, if (parameterLiteral.isBlank()) parameterLiteral else ", $parameterLiteral").build())

        fileBuilder.addFunction(FunSpec.builder(name)
                .receiver(SUPPORT_FRAGMENT.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(returnType)
                .addParameters(parameterSpecs)
                .addStatement("return (view?.parent as? %T)?.%L(%L)", VIEW_GROUP.kotlin, name, parameterLiteral).build())
    }
}
