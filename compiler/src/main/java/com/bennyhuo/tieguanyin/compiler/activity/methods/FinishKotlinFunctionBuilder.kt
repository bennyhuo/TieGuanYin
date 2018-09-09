package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.annotations.PendingTransition
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.types.ACTIVITY_COMPAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName

class FinishKotlinFunctionBuilder(private val activityClass: ActivityClass) {

    fun build(fileSpecBuilder: FileSpec.Builder) {
        val funBuilder = FunSpec.builder("finishWithTransition")
                .receiver(activityClass.type.asType().asTypeName())
                .addStatement("%T.finishAfterTransition(this)", ACTIVITY_COMPAT.kotlin)

        val pendingTransitionOnFinish = activityClass.pendingTransitionOnFinish
        if (pendingTransitionOnFinish.exitAnim != PendingTransition.DEFAULT || pendingTransitionOnFinish.enterAnim != PendingTransition.DEFAULT) {
            funBuilder.addStatement("overridePendingTransition(%L, %L)", pendingTransitionOnFinish.enterAnim, pendingTransitionOnFinish.exitAnim)
        }
        fileSpecBuilder.addFunction(funBuilder.build())
    }
}