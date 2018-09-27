package com.bennyhuo.tieguanyin.compiler.activity.builders

import com.bennyhuo.aptutils.types.asJavaTypeName
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.types.ACTIVITY_COMPAT
import com.bennyhuo.tieguanyin.compiler.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.utils.isDefault
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class FinishMethodBuilder(private val activityClass: ActivityClass) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val finishMethodBuilder = MethodSpec.methodBuilder("smartFinish")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(activityClass.type.asType().asJavaTypeName(), "activity")

        //handle result parameters.
        activityClass.activityResultClass?.resultParameters?.also{
            if(it.isNotEmpty()){
                finishMethodBuilder.addStatement("\$T intent = new \$T()", INTENT.java, INTENT.java)
                        .addStatement("activity.setResult(1, intent)")
            }
        }?.forEach {
            resultEntity ->
            finishMethodBuilder.addParameter(resultEntity.javaTypeName, resultEntity.name)
            finishMethodBuilder.addStatement("intent.putExtra(\$S, \$L)", resultEntity.name, resultEntity.name)
        }

        finishMethodBuilder.addStatement("\$T.finishAfterTransition(activity)", ACTIVITY_COMPAT.java)

        //handle pending transitions.
        val pendingTransitionOnFinish = activityClass.pendingTransitionOnFinish
        if (!pendingTransitionOnFinish.isDefault()) {
            finishMethodBuilder.addStatement("activity.overridePendingTransition(\$L, \$L)", pendingTransitionOnFinish.enterAnim, pendingTransitionOnFinish.exitAnim)
        }
        typeBuilder.addMethod(finishMethodBuilder.build())
    }
}