package com.bennyhuo.tieguanyin.compiler.fragment.builder

import com.bennyhuo.aptutils.types.asKotlinTypeName
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.basic.types.*
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.fragment.builder.Op.ADD
import com.bennyhuo.tieguanyin.compiler.fragment.builder.Op.REPLACE
import com.squareup.kotlinpoet.*

/**
 * Created by benny on 1/31/18.
 */

abstract class StartFragmentKFunctionBuilder(private val fragmentClass: FragmentClass) {

    abstract val name: String
    abstract val op: Op

    open fun build(fileBuilder: FileSpec.Builder) {
        val isReplace = op == REPLACE
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
            val template = field.kotlinTemplateToBundle(suggestedGetterName = name)
            funBuilderOfContext.addStatement("intent.putExtra(%S, ${template.first})", name, *template.second)
        }

        funBuilderOfContext.addParameter(ParameterSpec.builder("tag", STRING.kotlin.asNullable()).defaultValue("null").build())

        val sharedElements = fragmentClass.sharedElements
        if (sharedElements.isEmpty()) {
            funBuilderOfContext.addStatement("return %T.showFragment(this, %L, containerId, tag, intent.getExtras(), %T::class.java, null)", FRAGMENT_BUILDER.kotlin, isReplace, fragmentClass.typeElement)
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
            funBuilderOfContext.addStatement("return %T.showFragment(this, %L, containerId, tag, intent.getExtras(), %T::class.java, sharedElements)", FRAGMENT_BUILDER.kotlin, isReplace, fragmentClass.typeElement)
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

        when(op){
            Op.ADD -> {
                fileBuilder.addFunction(FunSpec.builder(name)
                        .receiver(SUPPORT_ACTIVITY.kotlin)
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
    override val name: String = "replace" + fragmentClass.simpleName
    override val op: Op = REPLACE
}

class AddKFunctionBuilder(fragmentClass: FragmentClass): StartFragmentKFunctionBuilder(fragmentClass) {
    override val name: String = "add" + fragmentClass.simpleName
    override val op: Op = ADD
}
