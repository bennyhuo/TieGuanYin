package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.lang.model.element.Modifier

/**
 * Created by benny on 1/31/18.
 */

class StartMethod(private val activityClass: ActivityClass, private val name: String) {
    private val requiredFields = ArrayList<Field>()
    private var isStaticMethod = true

    fun staticMethod(staticMethod: Boolean): StartMethod {
        isStaticMethod = staticMethod
        return this
    }

    fun addAllFields(requiredFields: List<Field>){
        this.requiredFields += requiredFields
    }

    fun addField(requiredField: Field) {
        this.requiredFields += requiredField
    }

    fun copy(name: String) = StartMethod(activityClass, name).also { it.requiredFields.addAll(this.requiredFields) }

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.CONTEXT, "context")
                .addStatement("\$T.INSTANCE.init(context)", JavaTypes.ACTIVITY_BUILDER)

        methodBuilder.addStatement("\$T intent = new \$T(context, \$T.class)", JavaTypes.INTENT, JavaTypes.INTENT, activityClass.type)

        val methodBuilderForView = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.VIEW, "view")
                .addStatement("\$T.INSTANCE.init(view.getContext())", JavaTypes.ACTIVITY_BUILDER)

        methodBuilderForView.addStatement("\$T intent = new \$T(view.getContext(), \$T.class)", JavaTypes.INTENT, JavaTypes.INTENT, activityClass.type)
        activityClass.categories.forEach { category ->
            methodBuilder.addStatement("intent.addCategory(\$S)", category)
            methodBuilderForView.addStatement("intent.addCategory(\$S)", category)
        }
        activityClass.flags.forEach { flag ->
            methodBuilder.addStatement("intent.addFlags(\$L)", flag)
            methodBuilderForView.addStatement("intent.addFlags(\$L)", flag)
        }

        requiredFields.forEach { requiredField ->
            val name = requiredField.name
            methodBuilder.addParameter(requiredField.asTypeName(), name)
                    .addStatement("intent.putExtra(\$S, \$L)", name, name)

            methodBuilderForView.addParameter(requiredField.asTypeName(), name)
                    .addStatement("intent.putExtra(\$S, \$L)", name, name)
        }

        if (isStaticMethod) {
            methodBuilder.addModifiers(Modifier.STATIC)
            methodBuilderForView.addModifiers(Modifier.STATIC)
        } else {
            //非静态则需要填充 optional 成员
            methodBuilder.addStatement("fillIntent(intent)")
            methodBuilderForView.addStatement("fillIntent(intent)")
        }

        methodBuilder.addStatement("\$T options = null", JavaTypes.BUNDLE)
        methodBuilderForView.addStatement("\$T options = null", JavaTypes.BUNDLE)
        val sharedElements = activityClass.sharedElements
        if (sharedElements.isNotEmpty()) {
            methodBuilderForView.addStatement("\$T<\$T<\$T, \$T>> sharedElements = new \$T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, JavaTypes.VIEW, String::class.java, JavaTypes.ARRAY_LIST)

            methodBuilder.beginControlFlow("if(context instanceof \$T)", JavaTypes.ACTIVITY)
                    .addStatement("\$T activity = (\$T) context", JavaTypes.ACTIVITY, JavaTypes.ACTIVITY)
                    .addStatement("\$T<\$T<\$T, \$T>> sharedElements = new \$T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, JavaTypes.VIEW, String::class.java, JavaTypes.ARRAY_LIST)

            var firstNeedTransitionNameMap = true
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceId == 0) {
                    if (firstNeedTransitionNameMap) {
                        methodBuilderForView.addStatement("\$T<\$T, \$T> nameMap = new \$T<>()", JavaTypes.HASH_MAP, String::class.java, JavaTypes.VIEW, JavaTypes.HASH_MAP)
                                .addStatement("\$T.findNamedViews(view, nameMap)", JavaTypes.VIEW_UTILS)
                        methodBuilder.addStatement("\$T<\$T, \$T> nameMap = new \$T<>()", JavaTypes.HASH_MAP, String::class.java, JavaTypes.VIEW, JavaTypes.HASH_MAP)
                                .addStatement("\$T.findNamedViews(activity.getWindow().getDecorView(), nameMap)", JavaTypes.VIEW_UTILS)
                        firstNeedTransitionNameMap = false
                    }

                    methodBuilder.addStatement("sharedElements.add(new Pair<>(nameMap.get(\$S), \$S))", sharedElement.sourceName, sharedElement.targetName)
                    methodBuilderForView.addStatement("sharedElements.add(new Pair<>(nameMap.get(\$S), \$S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(activity.findViewById(\$L), \$S))", sharedElement.sourceId, sharedElement.targetName)
                    methodBuilderForView.addStatement("sharedElements.add(new Pair<>(view.findViewById(\$L), \$S))", sharedElement.sourceId, sharedElement.targetName)
                }
            }

            methodBuilderForView.addStatement("options = \$T.makeSceneTransition(view.getContext(), sharedElements)", JavaTypes.ACTIVITY_BUILDER)

            methodBuilder.addStatement("options = \$T.makeSceneTransition(context, sharedElements)", JavaTypes.ACTIVITY_BUILDER)
                    .endControlFlow()
        }

        val pendingTransition = activityClass.pendingTransition
        val activityResultClass = activityClass.activityResultClass
        if (activityResultClass != null) {
            methodBuilder.addStatement("\$T.INSTANCE.startActivityForResult(context, intent, options, \$L, \$L, \$L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim, activityResultClass.createOnResultListenerObject())
                    .addParameter(activityResultClass.listenerClass, activityResultClass.listenerName, Modifier.FINAL)
        } else {
            methodBuilder.addStatement("\$T.INSTANCE.startActivity(context, intent, options, \$L, \$L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim)
        }

        if (activityResultClass != null) {
            methodBuilderForView.addStatement("\$T.INSTANCE.startActivityForResult(view.getContext(), intent, options, \$L, \$L, \$L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim, activityResultClass.createOnResultListenerObject())
                    .addParameter(activityResultClass.listenerClass, activityResultClass.listenerName, Modifier.FINAL)
        } else {
            methodBuilderForView.addStatement("\$T.INSTANCE.startActivity(view.getContext(), intent, options, \$L, \$L)", JavaTypes.ACTIVITY_BUILDER, pendingTransition.enterAnim, pendingTransition.exitAnim)
        }


        typeBuilder.addMethod(methodBuilder.build())
        typeBuilder.addMethod(methodBuilderForView.build())
    }
}
