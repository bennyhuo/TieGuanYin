package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.activity.entity.JavaOnResultListener
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_BUILDER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ARRAY_LIST
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.CONTEXT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.HASH_MAP
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.PAIR
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.STRING
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW_UTILS
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

/**
 * Created by benny on 1/31/18.
 */

class StartMethodBuilder(private val activityClass: ActivityClass, private val name: String) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilderOfContext = FunSpec.builder(name)
            .returns(UNIT)
            .addParameter("context", CONTEXT.kotlin)
            .addStatement("%T.INSTANCE.init(context)", ACTIVITY_BUILDER.kotlin)

        methodBuilderOfContext.addStatement(
            "val intent = %T(context, %T::class.java)",
            INTENT.kotlin,
            activityClass.typeElement.toKotlinTypeName()
        )

        val methodBuilderOfView = FunSpec.builder(name)
            .returns(UNIT)
            .addParameter("view", VIEW.kotlin)
            .addStatement(
                "val activity = %T.INSTANCE.findProperActivity(view)",
                ACTIVITY_BUILDER.kotlin
            )
            .beginControlFlow("if(activity != null)")

        activityClass.categories.forEach { category ->
            methodBuilderOfContext.addStatement("intent.addCategory(%S)", category)
        }
        activityClass.flags.forEach { flag ->
            methodBuilderOfContext.addStatement("intent.addFlags(%L)", flag)
        }
        methodBuilderOfContext.addStatement("fillIntent(intent)")

        val sharedElements = activityClass.sharedElements
        if (sharedElements.isNotEmpty()) {
            methodBuilderOfContext
                .beginControlFlow("val options = if(context is %T)", ACTIVITY.kotlin)
                .addStatement("val sharedElements = %T()", ARRAY_LIST[PAIR[VIEW, STRING]].kotlin)

            var firstNeedTransitionNameMap = true
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceId == 0 && sharedElement.sourceName != null) {
                    if (firstNeedTransitionNameMap) {
                        methodBuilderOfContext.addStatement(
                            "val nameMap = %T()",
                            HASH_MAP[STRING, VIEW].kotlin
                        )
                            .addStatement(
                                "%T.findNamedViews(context.window.decorView, nameMap)",
                                VIEW_UTILS.kotlin
                            )
                        firstNeedTransitionNameMap = false
                    }

                    methodBuilderOfContext.addStatement(
                        "sharedElements.add(Pair(nameMap.get(%S), %S))",
                        sharedElement.sourceName,
                        sharedElement.targetName
                    )
                } else {
                    methodBuilderOfContext.addStatement(
                        "sharedElements.add(Pair(context.findViewById(%L), %S))",
                        sharedElement.sourceId,
                        sharedElement.targetName
                    )
                }
            }
            methodBuilderOfContext.addStatement(
                "%T.makeSceneTransition(context, sharedElements)",
                ACTIVITY_BUILDER.kotlin
            ).nextControlFlow("else").addStatement("null")
                .endControlFlow()
        } else {
            methodBuilderOfContext.addStatement("val options = null")
        }

        val pendingTransition = activityClass.pendingTransition
        if (activityClass.hasResult) {
            val javaOnResultListener = JavaOnResultListener(activityClass)
            typeBuilder.addType(javaOnResultListener.buildInterface())

            methodBuilderOfContext.addStatement(
                "%T.INSTANCE.startActivityForResult(context, intent, options, %L, %L, %L)",
                ACTIVITY_BUILDER.kotlin,
                pendingTransition.enterAnim,
                pendingTransition.exitAnim,
                javaOnResultListener.buildObject()
            ).addParameter(
                ParameterSpec.builder(
                    javaOnResultListener.name,
                    javaOnResultListener.typeName.copy(nullable = true)
                ).defaultValue("null").build()
            )

            methodBuilderOfView.addParameter(
                javaOnResultListener.name,
                javaOnResultListener.typeName
            ).addStatement("%L(activity, %L)", name, javaOnResultListener.name)

        } else {
            methodBuilderOfContext.addStatement(
                "%T.INSTANCE.startActivity(context, intent, options, %L, %L)",
                ACTIVITY_BUILDER.kotlin,
                pendingTransition.enterAnim,
                pendingTransition.exitAnim
            )
            methodBuilderOfView.addStatement("%L(activity)", name)
        }

        typeBuilder.addFunction(methodBuilderOfContext.build())
        typeBuilder.addFunction(methodBuilderOfView.endControlFlow().build())
    }
}
