package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.activity.entity.KotlinOnResultListener
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_BUILDER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ARRAY_LIST
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.CONTEXT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.HASH_MAP
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.PAIR
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.STRING
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW_UTILS
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
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
            .addCode("%T.INSTANCE.init(this)", ACTIVITY_BUILDER.kotlin)
            .addCode(
                "val intent = %T(this, %T::class.java)",
                INTENT.kotlin,
                activityClass.typeElement.toKotlinTypeName()
            )

        activityClass.categories.forEach { category ->
            functionBuilderOfContext.addStatement("intent.addCategory(%S)", category)
        }
        activityClass.flags.forEach { flag ->
            functionBuilderOfContext.addStatement("intent.addFlags(%L)", flag)
        }

        activityClass.fields.forEach { requiredField ->
            functionBuilderOfContext.addParameter(
                ParameterSpec.builder(requiredField.name, requiredField.asKotlinTypeName())
                    .also { if (requiredField is OptionalField) it.defaultValue("null") }
                    .build())
                .addStatement("intent.putExtra(%S, %L)", requiredField.name, requiredField.name)
        }

        val sharedElements = activityClass.sharedElements
        val optionsName: String
        if (sharedElements.isEmpty()) {
            optionsName = "null"
        } else {
            optionsName = "options"
            functionBuilderOfContext.addStatement("var options: %T? = null", BUNDLE.kotlin)
                .beginControlFlow("if(this is %T)", ACTIVITY.kotlin)
                .addStatement("val sharedElements = %T()", ARRAY_LIST[PAIR[VIEW, STRING]].kotlin)

            var firstNeedTransitionNameMap = true
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceName != null) {
                    if (firstNeedTransitionNameMap) {
                        functionBuilderOfContext.addStatement(
                            "val nameMap = %T()",
                            HASH_MAP[STRING, VIEW].kotlin
                        )
                            .addStatement(
                                "%T.findNamedViews(window.decorView, nameMap)",
                                VIEW_UTILS.kotlin
                            )
                        firstNeedTransitionNameMap = false
                    }

                    functionBuilderOfContext.addStatement(
                        "sharedElements.add(Pair(nameMap[%S]!!, %S))",
                        sharedElement.sourceName,
                        sharedElement.targetName
                    )
                } else {
                    functionBuilderOfContext.addStatement(
                        "sharedElements.add(Pair(findViewById(%L), %S))",
                        sharedElement.sourceId,
                        sharedElement.targetName
                    )
                }
            }
            functionBuilderOfContext.addStatement(
                "options = %T.makeSceneTransition(this, sharedElements)",
                ACTIVITY_BUILDER.kotlin
            )
                .endControlFlow()

        }
        val pendingTransition = activityClass.pendingTransition
        if (activityClass.hasResult) {
            val kotlinOnResultListener = KotlinOnResultListener(activityClass)
            val listenerObject = kotlinOnResultListener.buildObject()

            functionBuilderOfContext
                .addStatement(
                    "%T.INSTANCE.startActivityForResult(this, intent, %L, %L, %L, %L)",
                    ACTIVITY_BUILDER.kotlin,
                    optionsName,
                    pendingTransition.enterAnim,
                    pendingTransition.exitAnim,
                    listenerObject
                )
                .addParameter(
                    ParameterSpec.builder(
                        kotlinOnResultListener.name,
                        kotlinOnResultListener.typeName
                    )
                        .defaultValue("null").build()
                )
        } else {
            functionBuilderOfContext.addStatement(
                "%T.INSTANCE.startActivity(this, intent, %L, %L, %L)",
                ACTIVITY_BUILDER.kotlin,
                optionsName,
                pendingTransition.enterAnim,
                pendingTransition.exitAnim
            )
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
