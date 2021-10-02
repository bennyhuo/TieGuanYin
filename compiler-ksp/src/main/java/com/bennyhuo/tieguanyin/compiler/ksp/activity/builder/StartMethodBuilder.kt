package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.activity.entity.JavaOnResultListener
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_BUILDER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ARRAY_LIST
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.CONTEXT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.HASH_MAP
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.PAIR
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.STRING
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW_UTILS
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Created by benny on 1/31/18.
 */

class StartMethodBuilder(private val activityClass: ActivityClass, private val name: String) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilderOfContext = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(CONTEXT.java, "context")
                .addStatement("\$T.INSTANCE.init(context)", ACTIVITY_BUILDER.java)

        methodBuilderOfContext.addStatement("\$T intent = new \$T(context, \$T.class)", INTENT.java, INTENT.java, activityClass.typeElement)

        val methodBuilderOfView = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(VIEW.java, "view")
                .addStatement("Activity activity = \$T.INSTANCE.findProperActivity(view)", ACTIVITY_BUILDER.java)
                .beginControlFlow("if(activity != null)")

        activityClass.categories.forEach { category ->
            methodBuilderOfContext.addStatement("intent.addCategory(\$S)", category)
        }
        activityClass.flags.forEach { flag ->
            methodBuilderOfContext.addStatement("intent.addFlags(\$L)", flag)
        }
        methodBuilderOfContext.addStatement("fillIntent(intent)")

        val sharedElements = activityClass.sharedElements
        val optionsName: String
        if (sharedElements.isNotEmpty()) {
            optionsName = "options"
            methodBuilderOfContext.addStatement("\$T options = null", BUNDLE.java)
                    .beginControlFlow("if(context instanceof \$T)", ACTIVITY.java)
                    .addStatement("\$T activity = (\$T) context", ACTIVITY.java, ACTIVITY.java)
                    .addStatement("\$T sharedElements = new \$T<>()", ARRAY_LIST[PAIR[VIEW, STRING]].java, ARRAY_LIST.java)

            var firstNeedTransitionNameMap = true
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceId == 0) {
                    if (firstNeedTransitionNameMap) {
                        methodBuilderOfContext.addStatement("\$T nameMap = new \$T<>()", HASH_MAP[STRING, VIEW].java, HASH_MAP.java)
                                .addStatement("\$T.findNamedViews(activity.getWindow().getDecorView(), nameMap)", VIEW_UTILS.java)
                        firstNeedTransitionNameMap = false
                    }

                    methodBuilderOfContext.addStatement("sharedElements.add(new Pair<>(nameMap.get(\$S), \$S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    methodBuilderOfContext.addStatement("sharedElements.add(new Pair<>(activity.findViewById(\$L), \$S))", sharedElement.sourceId, sharedElement.targetName)
                }
            }
            methodBuilderOfContext.addStatement("options = \$T.makeSceneTransition(context, sharedElements)", ACTIVITY_BUILDER.java)
                    .endControlFlow()
        } else {
            optionsName = "null"
        }

        val pendingTransition = activityClass.pendingTransition
        if (activityClass.hasResult) {
            val javaOnResultListener = JavaOnResultListener(activityClass)
            typeBuilder.addType(javaOnResultListener.buildInterface())

            methodBuilderOfContext.addStatement("\$T.INSTANCE.startActivityForResult(context, intent, \$L, \$L, \$L, \$L)", ACTIVITY_BUILDER.java, optionsName, pendingTransition.enterAnim, pendingTransition.exitAnim, javaOnResultListener.buildObject())
                    .addParameter(javaOnResultListener.typeName, javaOnResultListener.name, Modifier.FINAL)

            methodBuilderOfView.addParameter(javaOnResultListener.typeName, javaOnResultListener.name, Modifier.FINAL)
                    .addStatement("\$L(activity, \$L)", name, javaOnResultListener.name)
        } else {
            methodBuilderOfContext.addStatement("\$T.INSTANCE.startActivity(context, intent, \$L, \$L, \$L)", ACTIVITY_BUILDER.java, optionsName, pendingTransition.enterAnim, pendingTransition.exitAnim)
            methodBuilderOfView.addStatement("\$L(activity)", name)
        }

        typeBuilder.addMethod(methodBuilderOfContext.build())
        typeBuilder.addMethod(methodBuilderOfView.endControlFlow().build())
    }
}
