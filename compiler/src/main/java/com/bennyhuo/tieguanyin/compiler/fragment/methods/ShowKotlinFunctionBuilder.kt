package com.bennyhuo.tieguanyin.compiler.fragment.methods

import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClassBuilder
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes
import com.squareup.kotlinpoet.*

/**
 * Created by benny on 1/31/18.
 */

class ShowKotlinFunctionBuilder(private val fragmentClass: FragmentClass) {
    private val name = FragmentClassBuilder.METHOD_NAME + fragmentClass.simpleName

    fun build(fileBuilder: FileSpec.Builder) {
        val funBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_ACTIVITY)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)
                .addParameter("containerId", INT)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T()", KotlinTypes.INTENT)

        val funBuilderForViewGroup = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW_GROUP)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)

        val funBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.SUPPORT_FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)

        for (field in fragmentClass.fields) {
            val name = field.name
            val className = field.asKotlinTypeName()
            if (field is OptionalField) {
                funBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build())
            } else {
                funBuilderForContext.addParameter(name, className)
            }
            funBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name)
        }

        val sharedElements = fragmentClass.sharedElements
        if (sharedElements.isEmpty()) {
            funBuilderForContext.addStatement("%T.showFragment(this, containerId, intent.getExtras(), %T::class.java, null)", KotlinTypes.FRAGMENT_BUILDER, fragmentClass.type)
        } else {
            funBuilderForContext.addStatement("val sharedElements = %T<%T<%T, %T>>()", KotlinTypes.ARRAY_LIST, KotlinTypes.SUPPORT_PAIR, KotlinTypes.STRING, KotlinTypes.STRING)
                    .addStatement("val container: %T = findViewById(containerId)", KotlinTypes.VIEW)
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceName != null) {
                    funBuilderForContext.addStatement("sharedElements.add(Pair(%S, %S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    funBuilderForContext.addStatement("sharedElements.add(Pair(%T.getTransitionName(container.findViewById(%L)), %S))", KotlinTypes.VIEW_COMPAT, sharedElement.sourceId, sharedElement.targetName)
                }
            }
            funBuilderForContext.addStatement("%T.showFragment(this, containerId, intent.getExtras(), %T::class.java, sharedElements)", KotlinTypes.FRAGMENT_BUILDER, fragmentClass.type)
        }
        val paramBuilder = StringBuilder()
        val parameterSpecList = funBuilderForContext.parameters
        for (i in 1 until parameterSpecList.size) {
            val parameterSpec = parameterSpecList.get(i)
            paramBuilder.append(parameterSpec.name).append(",")
            funBuilderForViewGroup.addParameter(parameterSpec)
            funBuilderForFragment.addParameter(parameterSpec)
        }
        if (paramBuilder.isNotEmpty()) {
            paramBuilder.deleteCharAt(paramBuilder.length - 1)
        }
        funBuilderForFragment.addStatement("(view?.parent as? %T)?.%L(%L)", KotlinTypes.VIEW_GROUP, name, paramBuilder.toString())
        if (paramBuilder.isNotEmpty()) {
            paramBuilder.insert(0, ',')
        }
        funBuilderForViewGroup.addStatement("(context as? %T)?.%L(id %L)", KotlinTypes.SUPPORT_ACTIVITY, name, paramBuilder.toString())

        fileBuilder.addFunction(funBuilderForContext.build())
        fileBuilder.addFunction(funBuilderForViewGroup.build())
        fileBuilder.addFunction(funBuilderForFragment.build())
    }
}
