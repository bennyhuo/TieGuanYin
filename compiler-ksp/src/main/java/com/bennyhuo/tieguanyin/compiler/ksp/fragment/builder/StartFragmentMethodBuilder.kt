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
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toKotlinTypeName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec


/**
 * Created by benny on 1/31/18.
 */
enum class Op {
    ADD, REPLACE
}

abstract class StartFragmentMethodBuilder(protected val fragmentClass: FragmentClass) {

    abstract val name: String
    abstract val op: Op

    open fun build(typeBuilder: TypeSpec.Builder) {
        val isReplace = op == REPLACE

        val methodBuilder = FunSpec.builder(name)
            .returns(fragmentClass.declaration.toKotlinTypeName().copy(nullable = true))
            .addParameter("activity", ACTIVITY.kotlin)
            .addParameter("containerId", Int::class)
            .addParameter("tag", STRING.kotlin.copy(nullable = true))
            .beginControlFlow("if(activity is %T)", FRAGMENT_ACTIVITY.kotlin)
            .addStatement("%T.INSTANCE.init(activity)", ACTIVITY_BUILDER.kotlin)

        methodBuilder.addStatement("val intent = %T()", INTENT.kotlin)

        methodBuilder.addStatement("fillIntent(intent)")

        if (fragmentClass.sharedElements.isEmpty()) {
            methodBuilder.addStatement(
                "return %T.showFragment(activity, %L, containerId, tag, intent.extras, %T::class.java, null)",
                FRAGMENT_BUILDER.kotlin,
                isReplace,
                fragmentClass.declaration.toKotlinTypeName()
            )
        } else {
            methodBuilder.addStatement(
                "val sharedElements = %T()",
                ARRAY_LIST[PAIR[STRING, STRING]].kotlin
            )
                .addStatement("val container = activity.findViewById<%T>(containerId)", VIEW.kotlin)
            for (sharedElement in fragmentClass.sharedElements) {
                if (sharedElement.sourceId == 0 && sharedElement.sourceName != null) {
                    methodBuilder.addStatement(
                        "sharedElements.add(Pair(%S, %S))",
                        sharedElement.sourceName,
                        sharedElement.targetName
                    )
                } else {
                    methodBuilder.addStatement(
                        "sharedElements.add(Pair(%T.getTransitionName(container.findViewById(%L)), %S))",
                        VIEW_COMPAT.kotlin,
                        sharedElement.sourceId,
                        sharedElement.targetName
                    )
                }
            }
            methodBuilder.addStatement(
                "return %T.showFragment(activity, %L, containerId, tag, intent.getExtras(), %T::class.java, sharedElements)",
                FRAGMENT_BUILDER.kotlin,
                isReplace,
                fragmentClass.declaration.toKotlinTypeName()
            )
        }
        methodBuilder.endControlFlow()
            .addStatement("return null")

        typeBuilder.addFunction(methodBuilder.build())
        typeBuilder.addFunction(
            FunSpec.builder(name)
                .returns(fragmentClass.declaration.toKotlinTypeName().copy(nullable = true))
                .addParameter("activity", ACTIVITY.kotlin)
                .addParameter("containerId", Int::class)
                .addStatement("return %L(activity, containerId, null)", name).build()
        )
    }

}

class ReplaceMethodBuilder(fragmentClass: FragmentClass) :
    StartFragmentMethodBuilder(fragmentClass) {
    override val name: String = "replace"
    override val op: Op = REPLACE
}

class AddMethodBuilder(fragmentClass: FragmentClass) : StartFragmentMethodBuilder(fragmentClass) {
    override val name: String = "add"
    override val op: Op = ADD

    override fun build(typeBuilder: TypeSpec.Builder) {
        super.build(typeBuilder)
        //  You can provide tag only when "add" a fragment.
        typeBuilder.addFunction(
            FunSpec.builder(name)
                .returns(fragmentClass.declaration.toKotlinTypeName().copy(nullable = true))
                .addParameter("activity", ACTIVITY.kotlin)
                .addParameter("tag", STRING.kotlin)
                .addStatement("return %L(activity, 0, tag)", name).build()
        )
    }
}

