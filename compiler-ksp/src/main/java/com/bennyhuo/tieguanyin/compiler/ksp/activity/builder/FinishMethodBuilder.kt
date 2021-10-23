package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_COMPAT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.isDefault
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

class FinishMethodBuilder(private val activityClass: ActivityClass) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val finishMethodBuilder = FunSpec.builder("smartFinish")
            .addAnnotation(JvmStatic::class)
            .returns(UNIT)
            .addParameter("activity", activityClass.declaration.toKotlinTypeName())

        //handle result parameters.
        activityClass.resultParameters.also {
            if (it.isNotEmpty()) {
                finishMethodBuilder.addStatement("val intent = %T()", INTENT.kotlin)
                    .addStatement("activity.setResult(1, intent)")
            }
        }.forEach { resultParameter ->
            finishMethodBuilder.addParameter(resultParameter.name, resultParameter.kotlinTypeName)
            finishMethodBuilder.addStatement(
                "intent.putExtra(%S, %L)",
                resultParameter.name,
                resultParameter.name
            )
        }

        finishMethodBuilder.addStatement(
            "%T.finishAfterTransition(activity)",
            ACTIVITY_COMPAT.kotlin
        )

        //handle pending transitions.
        val pendingTransitionOnFinish = activityClass.pendingTransitionOnFinish
        if (!pendingTransitionOnFinish.isDefault()) {
            finishMethodBuilder.addStatement(
                "activity.overridePendingTransition(%L, %L)",
                pendingTransitionOnFinish.enterAnim,
                pendingTransitionOnFinish.exitAnim
            )
        }
        typeBuilder.addFunction(finishMethodBuilder.build())
    }
}