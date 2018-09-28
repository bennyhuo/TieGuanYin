package com.bennyhuo.tieguanyin.compiler.activity.builder

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.activity.entity.KotlinOnResultListener
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.basic.types.*
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
                .receiver(CONTEXT.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)
                .addStatement("%T.INSTANCE.init(this)", ACTIVITY_BUILDER.kotlin)
                .addStatement("val intent = %T(this, %T::class.java)", INTENT.kotlin, activityClass.type)

        val funBuilderForView = FunSpec.builder(name)
                .receiver(VIEW.kotlin)
                .addModifiers(KModifier.PUBLIC)
                .returns(Unit::class.java)
                .addStatement("%T.INSTANCE.init(context)", ACTIVITY_BUILDER.kotlin)
                .addStatement("val intent = %T(context, %T::class.java)", INTENT.kotlin, activityClass.type)

        val funBuilderForFragment = FunSpec.builder(name)
                .receiver(FRAGMENT.kotlin)
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

        funBuilderForContext.addStatement("var options: %T? = null", BUNDLE.kotlin)
        funBuilderForView.addStatement("var options: %T? = null", BUNDLE.kotlin)

        val sharedElements = activityClass.sharedElements
        if (sharedElements.size > 0) {
            funBuilderForView.addStatement("val sharedElements = %T()", ARRAY_LIST[SUPPORT_PAIR[VIEW, STRING]].kotlin)

            funBuilderForContext.beginControlFlow("if(this is %T)", ACTIVITY.kotlin)
            funBuilderForContext.addStatement("val sharedElements = %T()", ARRAY_LIST[SUPPORT_PAIR[VIEW, STRING]].kotlin)

            var firstNeedTransitionNameMap = true
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceName != null) {
                    if (firstNeedTransitionNameMap) {
                        funBuilderForView.addStatement("val nameMap = %T()", HASH_MAP[STRING, VIEW].kotlin)
                                .addStatement("%T.findNamedViews(this, nameMap)", VIEW_UTILS.kotlin)
                        funBuilderForContext.addStatement("val nameMap = %T()", HASH_MAP[STRING, VIEW].kotlin)
                                .addStatement("%T.findNamedViews(window.decorView, nameMap)", VIEW_UTILS.kotlin)
                        firstNeedTransitionNameMap = false
                    }

                    funBuilderForContext.addStatement("sharedElements.add(Pair(nameMap[%S]!!, %S))", sharedElement.sourceName, sharedElement.targetName)
                    funBuilderForView.addStatement("sharedElements.add(Pair(nameMap[%S]!!, %S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    funBuilderForContext.addStatement("sharedElements.add(Pair(findViewById(%L), %S))", sharedElement.sourceId, sharedElement.targetName)
                    funBuilderForView.addStatement("sharedElements.add(Pair(findViewById(%L), %S))", sharedElement.sourceId, sharedElement.targetName)
                }
            }
            funBuilderForContext.addStatement("options = %T.makeSceneTransition(this, sharedElements)", ACTIVITY_BUILDER.kotlin)
            funBuilderForContext.endControlFlow()

            funBuilderForView.addStatement("options = %T.makeSceneTransition(context, sharedElements)", ACTIVITY_BUILDER.kotlin)
        }
        val pendingTransition = activityClass.pendingTransition
        if (activityClass.hasResult) {
            val kotlinOnResultListener = KotlinOnResultListener(activityClass)
            val listenerObject = kotlinOnResultListener.buildObject()

            funBuilderForContext
                    .addStatement("%T.INSTANCE.startActivityForResult(this, intent, options, %L, %L, %L)", ACTIVITY_BUILDER.kotlin, pendingTransition.enterAnim, pendingTransition.exitAnim, listenerObject)
                    .addParameter(
                            ParameterSpec.builder(kotlinOnResultListener.name, kotlinOnResultListener.typeName)
                                    .defaultValue("null").build())

            funBuilderForView
                    .addStatement("%T.INSTANCE.startActivityForResult(context, intent, options, %L, %L, %L)", ACTIVITY_BUILDER.kotlin, pendingTransition.enterAnim, pendingTransition.exitAnim, listenerObject)
                    .addParameter(
                            ParameterSpec.builder(kotlinOnResultListener.name, kotlinOnResultListener.typeName)
                                    .defaultValue("null").build())
        } else {
            funBuilderForContext.addStatement("%T.INSTANCE.startActivity(this, intent, options, %L, %L)", ACTIVITY_BUILDER.kotlin, pendingTransition.enterAnim, pendingTransition.exitAnim)
            funBuilderForView.addStatement("%T.INSTANCE.startActivity(context, intent, options, %L, %L)", ACTIVITY_BUILDER.kotlin, pendingTransition.enterAnim, pendingTransition.exitAnim)
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
