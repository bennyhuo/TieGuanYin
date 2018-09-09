package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.utils.KotlinTypes
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec

/**
 * Created by benny on 1/31/18.
 */

class StartKotlinFunctionBuilder(private val activityClass: ActivityClass) {

    private val name = ActivityClassBuilder.METHOD_NAME + activityClass.simpleName

    fun build(fileBuilder: FileSpec.Builder) {
        val funBuilderForContext = FunSpec.builder(name)
                .receiver(KotlinTypes.CONTEXT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)
                .addStatement("%T.INSTANCE.init(this)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T(this, %T::class.java)", KotlinTypes.INTENT, activityClass.type)

        val funBuilderForView = FunSpec.builder(name)
                .receiver(KotlinTypes.VIEW)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)
                .addStatement("%T.INSTANCE.init(context)", KotlinTypes.ACTIVITY_BUILDER)
                .addStatement("val intent = %T(context, %T::class.java)", KotlinTypes.INTENT, activityClass.type)

        val funBuilderForFragment = FunSpec.builder(name)
                .receiver(KotlinTypes.FRAGMENT)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)

        activityClass.categories.forEach { category ->
            funBuilderForContext.addStatement("intent.addCategory(%S)", category)
            funBuilderForView.addStatement("intent.addCategory(%S)", category)
        }
        activityClass.flags.forEach { flag ->
            funBuilderForContext.addStatement("intent.addFlags(%L)", flag)
            funBuilderForView.addStatement("intent.addFlags(%L)", flag)
        }

        activityClass.fields.forEach { requiredField ->
            val name = requiredField.name
            val className = requiredField.asKotlinTypeName()
            if (requiredField is OptionalField) {
                funBuilderForContext.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build())
                funBuilderForView.addParameter(ParameterSpec.builder(name, className).defaultValue("null").build())
            } else {
                funBuilderForContext.addParameter(name, className)
                funBuilderForView.addParameter(name, className)
            }
            funBuilderForContext.addStatement("intent.putExtra(%S, %L)", name, name)
            funBuilderForView.addStatement("intent.putExtra(%S, %L)", name, name)
        }

        funBuilderForContext.addStatement("var options: %T? = null", KotlinTypes.BUNDLE)
        funBuilderForView.addStatement("var options: %T? = null", KotlinTypes.BUNDLE)

        val sharedElements = activityClass.sharedElements
        if (sharedElements.size > 0) {
            funBuilderForView.addStatement("val sharedElements = %T<%T<%T, %T>>()", KotlinTypes.ARRAY_LIST, KotlinTypes.SUPPORT_PAIR, KotlinTypes.VIEW, KotlinTypes.STRING)

            funBuilderForContext.beginControlFlow("if(this is %T)", KotlinTypes.ACTIVITY)
            funBuilderForContext.addStatement("val sharedElements = %T<%T<%T, %T>>()", KotlinTypes.ARRAY_LIST, KotlinTypes.SUPPORT_PAIR, KotlinTypes.VIEW, KotlinTypes.STRING)

            var firstNeedTransitionNameMap = true
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceName != null) {
                    if (firstNeedTransitionNameMap) {
                        funBuilderForView.addStatement("val nameMap = %T<%T, %T>()", KotlinTypes.HASH_MAP, KotlinTypes.STRING, KotlinTypes.VIEW)
                                .addStatement("%T.findNamedViews(this, nameMap)", KotlinTypes.VIEW_UTILS)
                        funBuilderForContext.addStatement("val nameMap = %T<%T, %T>()", KotlinTypes.HASH_MAP, KotlinTypes.STRING, KotlinTypes.VIEW)
                                .addStatement("%T.findNamedViews(window.decorView, nameMap)", KotlinTypes.VIEW_UTILS)
                        firstNeedTransitionNameMap = false
                    }

                    funBuilderForContext.addStatement("sharedElements.add(Pair(nameMap[%S]!!, %S))", sharedElement.sourceName, sharedElement.targetName)
                    funBuilderForView.addStatement("sharedElements.add(Pair(nameMap[%S]!!, %S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    funBuilderForContext.addStatement("sharedElements.add(Pair(findViewById(%L), %S))", sharedElement.sourceId, sharedElement.targetName)
                    funBuilderForView.addStatement("sharedElements.add(Pair(findViewById(%L), %S))", sharedElement.sourceId, sharedElement.targetName)
                }
            }
            funBuilderForContext.addStatement("options = %T.makeSceneTransition(this, sharedElements)", KotlinTypes.ACTIVITY_BUILDER)
            funBuilderForContext.endControlFlow()

            funBuilderForView.addStatement("options = %T.makeSceneTransition(context, sharedElements)", KotlinTypes.ACTIVITY_BUILDER)
        }
        val pendingTransition = activityClass.pendingTransition
        val activityResultClass = activityClass.activityResultClass
        if (activityResultClass != null) {
            funBuilderForContext
                    .addStatement("%T.INSTANCE.startActivityForResult(this, intent, options, %L, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim, activityResultClass.createOnResultListenerObjectKt())
                    .addParameter(
                            ParameterSpec.builder(activityResultClass.listenerName, activityResultClass.listenerClassKt)
                                    .defaultValue("null").build())
        } else {
            funBuilderForContext.addStatement("%T.INSTANCE.startActivity(this, intent, options, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim)
        }

        if (activityResultClass != null) {
            funBuilderForView
                    .addStatement("%T.INSTANCE.startActivityForResult(context, intent, options, %L, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim, activityResultClass.createOnResultListenerObjectKt())
                    .addParameter(
                            ParameterSpec.builder(activityResultClass.listenerName, activityResultClass.listenerClassKt)
                                    .defaultValue("null").build())
        } else {
            funBuilderForView.addStatement("%T.INSTANCE.startActivity(context, intent, options, %L, %L)", KotlinTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim)
        }

        val paramBuilder = StringBuilder()
        for (parameterSpec in funBuilderForContext.parameters) {
            paramBuilder.append(parameterSpec.name).append(",")
            funBuilderForFragment.addParameter(parameterSpec)
        }
        if (paramBuilder.isNotEmpty()) {
            paramBuilder.deleteCharAt(paramBuilder.length - 1)
        }
        funBuilderForFragment.addStatement("view?.%L(%L)", name, paramBuilder.toString())

        fileBuilder.addFunction(funBuilderForContext.build())
        fileBuilder.addFunction(funBuilderForView.build())
        fileBuilder.addFunction(funBuilderForFragment.build())
    }
}
