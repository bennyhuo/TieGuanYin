package com.bennyhuo.tieguanyin.compiler.fragment.builder

import com.bennyhuo.tieguanyin.compiler.basic.types.*
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Created by benny on 1/31/18.
 */

class ShowMethodBuilder(private val fragmentClass: FragmentClass, private val name: String) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(ACTIVITY.java, "activity")
                .addParameter(Int::class.javaPrimitiveType, "containerId")
                .beginControlFlow("if(activity instanceof \$T)", SUPPORT_ACTIVITY.java)
                .addStatement("\$T.INSTANCE.init(activity)", ACTIVITY_BUILDER.java)

        methodBuilder.addStatement("\$T intent = new \$T()", INTENT.java, INTENT.java)

         methodBuilder.addStatement("fillIntent(intent)")

        if (fragmentClass.sharedElements.isEmpty()) {
            methodBuilder.addStatement("\$T.showFragment((\$T) activity, containerId, intent.getExtras(), \$T.class, null)", FRAGMENT_BUILDER.java, SUPPORT_ACTIVITY.java, fragmentClass.type)
        } else {
            methodBuilder.addStatement("\$T sharedElements = new \$T<>()", ARRAY_LIST[SUPPORT_PAIR[STRING, STRING]].java, ARRAY_LIST.java)
                    .addStatement("\$T container = activity.findViewById(containerId)", VIEW.java)
            for (sharedElement in fragmentClass.sharedElements) {
                if (sharedElement.sourceId == 0) {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(\$S, \$S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(\$T.getTransitionName(container.findViewById(\$L)), \$S))", VIEW_COMPAT.java, sharedElement.sourceId, sharedElement.targetName)
                }
            }
            methodBuilder.addStatement("\$T.showFragment((\$T) activity, containerId, intent.getExtras(), \$T.class, sharedElements)", FRAGMENT_BUILDER.java, SUPPORT_ACTIVITY.java, fragmentClass.type)
        }
        methodBuilder.endControlFlow()

        typeBuilder.addMethod(methodBuilder.build())
    }
}
