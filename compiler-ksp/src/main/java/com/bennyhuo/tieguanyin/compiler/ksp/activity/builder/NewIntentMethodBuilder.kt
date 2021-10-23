package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

class NewIntentMethodBuilder(private val activityClass: ActivityClass) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val newIntentMethodBuilder = FunSpec.builder("processNewIntent")
            .addAnnotation(JvmStatic::class)
            .returns(UNIT)
            .addParameter("activity", activityClass.declaration.toKotlinTypeName())
            .addParameter("intent", INTENT.kotlin.copy(nullable = true))

        newIntentMethodBuilder.addStatement("processNewIntent(activity, intent, true)")
        typeBuilder.addFunction(newIntentMethodBuilder.build())

        val newIntentWithUpdateIntentMethodBuilder = FunSpec.builder("processNewIntent")
            .addAnnotation(JvmStatic::class)
            .returns(UNIT)
            .addParameter("activity", activityClass.declaration.toKotlinTypeName())
            .addParameter("intent", INTENT.kotlin.copy(nullable = true))
            .addParameter("updateIntent", Boolean::class)

        //update intent.
        newIntentWithUpdateIntentMethodBuilder.beginControlFlow("if(updateIntent)")
            .addStatement("activity.setIntent(intent)")
            .endControlFlow()

        newIntentWithUpdateIntentMethodBuilder.beginControlFlow("if(intent != null)")
            .addStatement("inject(activity, intent.getExtras())")
            .endControlFlow()

        typeBuilder.addFunction(newIntentWithUpdateIntentMethodBuilder.build())
    }
}