package com.bennyhuo.tieguanyin.compiler.activity.builder

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.activity.entity.JavaOnResultListener
import com.bennyhuo.tieguanyin.compiler.basic.types.*
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Created by benny on 1/31/18.
 */

class StartMethodBuilder(private val activityClass: ActivityClass, private val name: String) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(CONTEXT.java, "context")
                .addStatement("\$T.INSTANCE.init(context)", ACTIVITY_BUILDER.java)

        methodBuilder.addStatement("\$T intent = new \$T(context, \$T.class)", INTENT.java, INTENT.java, activityClass.type)

        val methodBuilderForView = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(VIEW.java, "view")
                .addStatement("Activity activity = \$T.INSTANCE.findProperActivity(view)", ACTIVITY_BUILDER.java)
                .beginControlFlow("if(activity != null)")

        activityClass.categories.forEach { category ->
            methodBuilder.addStatement("intent.addCategory(\$S)", category)
        }
        activityClass.flags.forEach { flag ->
            methodBuilder.addStatement("intent.addFlags(\$L)", flag)
        }
        methodBuilder.addStatement("fillIntent(intent)")
        methodBuilder.addStatement("\$T options = null", BUNDLE.java)
        val sharedElements = activityClass.sharedElements
        if (sharedElements.isNotEmpty()) {
            methodBuilder.beginControlFlow("if(context instanceof \$T)", ACTIVITY.java)
                    .addStatement("\$T activity = (\$T) context", ACTIVITY.java, ACTIVITY.java)
                    .addStatement("\$T sharedElements = new \$T<>()", ARRAY_LIST[SUPPORT_PAIR[VIEW, STRING]].java, ARRAY_LIST.java)

            var firstNeedTransitionNameMap = true
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceId == 0) {
                    if (firstNeedTransitionNameMap) {
                        methodBuilder.addStatement("\$T nameMap = new \$T<>()", HASH_MAP[STRING, VIEW].java, HASH_MAP.java)
                                .addStatement("\$T.findNamedViews(activity.getWindow().getDecorView(), nameMap)", VIEW_UTILS.java)
                        firstNeedTransitionNameMap = false
                    }

                    methodBuilder.addStatement("sharedElements.add(new Pair<>(nameMap.get(\$S), \$S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(activity.findViewById(\$L), \$S))", sharedElement.sourceId, sharedElement.targetName)
                }
            }
            methodBuilder.addStatement("options = \$T.makeSceneTransition(context, sharedElements)", ACTIVITY_BUILDER.java)
                    .endControlFlow()
        }

        val pendingTransition = activityClass.pendingTransition
        if (activityClass.hasResult) {
            val javaOnResultListener = JavaOnResultListener(activityClass)
            typeBuilder.addType(javaOnResultListener.buildInterface())

            methodBuilder.addStatement("\$T.INSTANCE.startActivityForResult(context, intent, options, \$L, \$L, \$L)", ACTIVITY_BUILDER.java, pendingTransition.enterAnim, pendingTransition.exitAnim, javaOnResultListener.buildObject())
                    .addParameter(javaOnResultListener.typeName, javaOnResultListener.name, Modifier.FINAL)

            methodBuilderForView.addParameter(javaOnResultListener.typeName, javaOnResultListener.name, Modifier.FINAL)
                    .addStatement("start(activity, \$L)", javaOnResultListener.name)
        } else {
            methodBuilder.addStatement("\$T.INSTANCE.startActivity(context, intent, options, \$L, \$L)", ACTIVITY_BUILDER.java, pendingTransition.enterAnim, pendingTransition.exitAnim)
            methodBuilderForView.addStatement("start(activity)")
        }

        typeBuilder.addMethod(methodBuilder.build())
        typeBuilder.addMethod(methodBuilderForView.endControlFlow().build())
    }
}
