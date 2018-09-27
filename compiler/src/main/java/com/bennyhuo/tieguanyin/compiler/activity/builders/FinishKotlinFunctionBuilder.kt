package com.bennyhuo.tieguanyin.compiler.activity.builders

import com.bennyhuo.aptutils.types.asKotlinTypeName
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.types.ACTIVITY_COMPAT
import com.bennyhuo.tieguanyin.compiler.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.utils.isDefault
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec

class FinishKotlinFunctionBuilder(private val activityClass: ActivityClass) {

    fun build(fileSpecBuilder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("smartFinish")
                .receiver(activityClass.type.asType().asKotlinTypeName())

        activityClass.activityResultClass?.resultParameters?.also {
            if (it.isNotEmpty()) {
                funBuilder.addStatement("val intent = %T()", INTENT.kotlin)
                        .addStatement("setResult(1, intent)")
            }
        }?.forEach { resultEntity ->
            funBuilder.addParameter(resultEntity.name, resultEntity.kotlinTypeName)
                    .addStatement("intent.putExtra(%S, %L)", resultEntity.name, resultEntity.name)
        }

        funBuilder.addStatement("%T.finishAfterTransition(this)", ACTIVITY_COMPAT.kotlin)

        val pendingTransitionOnFinish = activityClass.pendingTransitionOnFinish
        if (!pendingTransitionOnFinish.isDefault()) {
            funBuilder.addStatement("overridePendingTransition(%L, %L)", pendingTransitionOnFinish.enterAnim, pendingTransitionOnFinish.exitAnim)
        }

        fileSpecBuilder.addFunction(funBuilder.build())
    }
}