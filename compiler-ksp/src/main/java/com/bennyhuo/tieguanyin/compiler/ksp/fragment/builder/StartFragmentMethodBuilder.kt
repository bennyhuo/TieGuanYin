package com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY_BUILDER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ARRAY_LIST
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT_ACTIVITY
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT_BUILDER
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.INTENT
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.PAIR
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.STRING
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.VIEW_COMPAT
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.Op.ADD
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder.Op.REPLACE
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toJavaTypeName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeSpec.Builder
import javax.lang.model.element.Modifier

/**
 * Created by benny on 1/31/18.
 */
enum class Op{
    ADD, REPLACE
}

abstract class StartFragmentMethodBuilder(protected val fragmentClass: FragmentClass){

    abstract val name: String
    abstract val op: Op

    open fun build(typeBuilder: TypeSpec.Builder) {
        val isReplace = op == REPLACE

        val methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(fragmentClass.typeElement.toJavaTypeName())
                .addParameter(ACTIVITY.java, "activity")
                .addParameter(Int::class.javaPrimitiveType, "containerId")
                .addParameter(STRING.java, "tag")
                .beginControlFlow("if(activity instanceof \$T)", FRAGMENT_ACTIVITY.java)
                .addStatement("\$T.INSTANCE.init(activity)", ACTIVITY_BUILDER.java)

        methodBuilder.addStatement("\$T intent = new \$T()", INTENT.java, INTENT.java)

        methodBuilder.addStatement("fillIntent(intent)")

        if (fragmentClass.sharedElements.isEmpty()) {
            methodBuilder.addStatement("return \$T.showFragment((\$T) activity, \$L, containerId, tag, intent.getExtras(), \$T.class, null)", FRAGMENT_BUILDER.java, FRAGMENT_ACTIVITY.java, isReplace, fragmentClass.typeElement)
        } else {
            methodBuilder.addStatement("\$T sharedElements = new \$T<>()", ARRAY_LIST[PAIR[STRING, STRING]].java, ARRAY_LIST.java)
                    .addStatement("\$T container = activity.findViewById(containerId)", VIEW.java)
            for (sharedElement in fragmentClass.sharedElements) {
                if (sharedElement.sourceId == 0) {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(\$S, \$S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(\$T.getTransitionName(container.findViewById(\$L)), \$S))", VIEW_COMPAT.java, sharedElement.sourceId, sharedElement.targetName)
                }
            }
            methodBuilder.addStatement("return \$T.showFragment((\$T) activity, \$L, containerId, tag, intent.getExtras(), \$T.class, sharedElements)", FRAGMENT_BUILDER.java, FRAGMENT_ACTIVITY.java, isReplace, fragmentClass.typeElement)
        }
        methodBuilder.endControlFlow()
                .addStatement("return null")

        typeBuilder.addMethod(methodBuilder.build())
        typeBuilder.addMethod(MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(fragmentClass.typeElement.toJavaTypeName())
                .addParameter(ACTIVITY.java, "activity")
                .addParameter(Int::class.javaPrimitiveType, "containerId")
                .addStatement("return \$L(activity, containerId, null)", name).build())
    }

}

class ReplaceMethodBuilder(fragmentClass: FragmentClass): StartFragmentMethodBuilder(fragmentClass) {
    override val name: String = "replace"
    override val op: Op = REPLACE
}

class AddMethodBuilder(fragmentClass: FragmentClass): StartFragmentMethodBuilder(fragmentClass) {
    override val name: String = "add"
    override val op: Op = ADD

    override fun build(typeBuilder: Builder) {
        super.build(typeBuilder)
        //  You can provide tag only when "add" a fragment.
        typeBuilder.addMethod(MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(fragmentClass.typeElement.toJavaTypeName())
                .addParameter(ACTIVITY.java, "activity")
                .addParameter(STRING.java, "tag")
                .addStatement("return \$L(activity, 0, tag)", name).build())
    }
}

