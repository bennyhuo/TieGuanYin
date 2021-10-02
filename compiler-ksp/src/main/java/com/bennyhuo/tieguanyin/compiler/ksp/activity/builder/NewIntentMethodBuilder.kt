package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toJavaTypeName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class NewIntentMethodBuilder(private val activityClass: ActivityClass) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val newIntentMethodBuilder = MethodSpec.methodBuilder("processNewIntent")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(activityClass.typeElement.toJavaTypeName(), "activity")
                .addParameter(INTENT.java, "intent")

        newIntentMethodBuilder.addStatement("processNewIntent(activity, intent, true)")
        typeBuilder.addMethod(newIntentMethodBuilder.build())

        val newIntentWithUpdateIntentMethodBuilder = MethodSpec.methodBuilder("processNewIntent")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(activityClass.typeElement.toJavaTypeName(), "activity")
                .addParameter(INTENT.java, "intent")
                .addParameter(Boolean::class.java, "updateIntent")

        //update intent.
        newIntentWithUpdateIntentMethodBuilder.beginControlFlow("if(updateIntent)")
                .addStatement("activity.setIntent(intent)")
                .endControlFlow()

        newIntentWithUpdateIntentMethodBuilder.beginControlFlow("if(intent != null)")
                .addStatement("inject(activity, intent.getExtras())")
                .endControlFlow()

        typeBuilder.addMethod(newIntentWithUpdateIntentMethodBuilder.build())
    }
}