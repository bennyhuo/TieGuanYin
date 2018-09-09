package com.bennyhuo.tieguanyin.compiler.fragment.methods

import com.bennyhuo.tieguanyin.compiler.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.shared.SharedElementEntity
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Created by benny on 1/31/18.
 */

class ShowMethod(private val enclosingElementType: TypeElement, sharedElements: List<SharedElementEntity>, private val name: String) {
    private val sharedElements = ArrayList<SharedElementEntity>()
    private val requiredFields = ArrayList<Field>()
    private var isStaticMethod = true

    init {
        this.sharedElements.addAll(sharedElements)
    }

    fun staticMethod(staticMethod: Boolean): ShowMethod {
        isStaticMethod = staticMethod
        return this
    }

    fun addAllFields(requiredFields: List<Field>){
        this.requiredFields += requiredFields
    }

    fun addField(requiredField: Field) {
        this.requiredFields += requiredField
    }

    fun copy(name: String) = ShowMethod(enclosingElementType, sharedElements, name).also { it.requiredFields.addAll(this.requiredFields) }

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(JavaTypes.ACTIVITY, "activity")
                .addParameter(Int::class.javaPrimitiveType, "containerId")
                .beginControlFlow("if(activity instanceof \$T)", JavaTypes.SUPPORT_ACTIVITY)
                .addStatement("\$T.INSTANCE.init(activity)", JavaTypes.ACTIVITY_BUILDER)

        methodBuilder.addStatement("\$T intent = new \$T()", JavaTypes.INTENT, JavaTypes.INTENT)

        for (requiredField in requiredFields) {
            val name = requiredField.name
            methodBuilder.addParameter(requiredField.asTypeName(), name)
            methodBuilder.addStatement("intent.putExtra(\$S, \$L)", name, name)
        }

        if (isStaticMethod) {
            methodBuilder.addModifiers(Modifier.STATIC)
        } else {
            //非静态则需要填充 optional 成员
            methodBuilder.addStatement("fillIntent(intent)")
        }

        if (sharedElements.isEmpty()) {
            methodBuilder.addStatement("\$T.showFragment((\$T) activity, containerId, intent.getExtras(), \$T.class, null)", JavaTypes.FRAGMENT_BUILDER, JavaTypes.SUPPORT_ACTIVITY, enclosingElementType)
        } else {
            methodBuilder.addStatement("\$T<\$T<\$T, \$T>> sharedElements = new \$T<>()", JavaTypes.ARRAY_LIST, JavaTypes.SUPPORT_PAIR, String::class.java, String::class.java, JavaTypes.ARRAY_LIST)
                    .addStatement("\$T container = activity.findViewById(containerId)", JavaTypes.VIEW)
            for (sharedElement in sharedElements) {
                if (sharedElement.sourceId == 0) {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(\$S, \$S))", sharedElement.sourceName, sharedElement.targetName)
                } else {
                    methodBuilder.addStatement("sharedElements.add(new Pair<>(\$T.getTransitionName(container.findViewById(\$L)), \$S))", JavaTypes.VIEW_COMPAT, sharedElement.sourceId, sharedElement.targetName)
                }
            }
            methodBuilder.addStatement("\$T.showFragment((\$T) activity, containerId, intent.getExtras(), \$T.class, sharedElements)", JavaTypes.FRAGMENT_BUILDER, JavaTypes.SUPPORT_ACTIVITY, enclosingElementType)
        }
        methodBuilder.endControlFlow()

        typeBuilder.addMethod(methodBuilder.build())
    }
}
