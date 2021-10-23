package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_COMPAT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.asType
import com.bennyhuo.tieguanyin.compiler.ksp.utils.isDefault
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toTypeName

class FinishKFunctionBuilder(private val activityClass: ActivityClass) {

    fun build(fileSpecBuilder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("smartFinish")
                .receiver(activityClass.declaration.asType().toTypeName())

        activityClass.resultParameters.also {
            if (it.isNotEmpty()) {
                funBuilder.addStatement("val intent = %T()", INTENT.kotlin)
                        .addStatement("setResult(1, intent)")
            }
        }.forEach { resultParameter ->
            funBuilder.addParameter(resultParameter.name, resultParameter.kotlinTypeName)
                    .addStatement("intent.putExtra(%S, %L)", resultParameter.name, resultParameter.name)
        }

        funBuilder.addStatement("%T.finishAfterTransition(this)", ACTIVITY_COMPAT.kotlin)

        val pendingTransitionOnFinish = activityClass.pendingTransitionOnFinish
        if (!pendingTransitionOnFinish.isDefault()) {
            funBuilder.addStatement("overridePendingTransition(%L, %L)", pendingTransitionOnFinish.enterAnim, pendingTransitionOnFinish.exitAnim)
        }

        fileSpecBuilder.addFunction(funBuilder.build())
    }
}