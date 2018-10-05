package com.bennyhuo.tieguanyin.compiler.fragment.builder

import com.bennyhuo.aptutils.types.asJavaTypeName
import com.bennyhuo.tieguanyin.compiler.basic.types.*
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.fragment.builder.Op.ADD
import com.bennyhuo.tieguanyin.compiler.fragment.builder.Op.REPLACE
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

/**
 * Created by benny on 1/31/18.
 */
enum class Op{
    ADD, REPLACE
}

abstract class StartFragmentMethodBuilder(private val fragmentClass: FragmentClass, private val op: Op){

    abstract val name: String

    fun build(typeBuilder: TypeSpec.Builder) {
        val isReplace = op == REPLACE

        val methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(fragmentClass.typeElement.asType().asJavaTypeName())
                .addParameter(ACTIVITY.java, "activity")
                .addParameter(Int::class.javaPrimitiveType, "containerId")
                .addParameter(STRING.java, "tag")
                .beginControlFlow("if(activity instanceof \$T)", SUPPORT_ACTIVITY.java)
                .addStatement("\$T.INSTANCE.init(activity)", ACTIVITY_BUILDER.java)

        methodBuilder.addStatement("\$T intent = new \$T()", INTENT.java, INTENT.java)

        methodBuilder.addStatement("fillIntent(intent)")

        if (fragmentClass.sharedElements.isEmpty()) {
            methodBuilder.addStatement("return \$T.showFragment((\$T) activity, \$L, containerId, tag, intent.getExtras(), \$T.class, null)", FRAGMENT_BUILDER.java, SUPPORT_ACTIVITY.java, isReplace, fragmentClass.typeElement)
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
            methodBuilder.addStatement("return \$T.showFragment((\$T) activity, \$L, containerId, tag, intent.getExtras(), \$T.class, sharedElements)", FRAGMENT_BUILDER.java, SUPPORT_ACTIVITY.java, isReplace, fragmentClass.typeElement)
        }
        methodBuilder.endControlFlow()
                .addStatement("return null")

        typeBuilder.addMethod(methodBuilder.build())
        typeBuilder.addMethod(MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(fragmentClass.typeElement.asType().asJavaTypeName())
                .addParameter(ACTIVITY.java, "activity")
                .addParameter(Int::class.javaPrimitiveType, "containerId")
                .addStatement("return \$L(activity, containerId, null)", name).build())
    }

}

class ReplaceMethodBuilder(fragmentClass: FragmentClass): StartFragmentMethodBuilder(fragmentClass, REPLACE) {
    override val name: String = "replace"
}

class AddMethodBuilder(fragmentClass: FragmentClass): StartFragmentMethodBuilder(fragmentClass, ADD) {
    override val name: String = "add"
}

