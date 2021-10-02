package com.bennyhuo.tieguanyin.compiler.ksp.basic.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.RUNTIME_UTILS
import com.bennyhuo.tieguanyin.compiler.ksp.utils.toJavaTypeName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

abstract class BasicInjectMethodBuilder(val basicClass: BasicClass) {

    abstract val instanceType: TypeName
    abstract val snippetToRetrieveState: String

    fun build(typeBuilder: TypeSpec.Builder) {
        val injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addParameter(instanceType, "instance")
                .addParameter(BUNDLE.java, "savedInstanceState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .beginControlFlow("if(instance instanceof \$T)", basicClass.typeElement.toJavaTypeName())
                .addStatement("\$T typedInstance = (\$T) instance", basicClass.typeElement.toJavaTypeName(), basicClass.typeElement.toJavaTypeName())
                .beginControlFlow("if(savedInstanceState != null)")

        for (field in basicClass.fields) {
            val name = field.name
            val typeName = field.asTypeName().box()

            when {
                field is OptionalField && field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.set\$L(\$T.<\$T>get(savedInstanceState, \$S, \$L))", name.capitalize(), RUNTIME_UTILS.java, typeName, name, field.defaultValue)
                }
                field is OptionalField && !field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.\$L = \$T.<\$T>get(savedInstanceState, \$S, \$L)", name, RUNTIME_UTILS.java, typeName, name, field.defaultValue)
                }
                field !is OptionalField && field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.set\$L(\$T.<\$T>get(savedInstanceState, \$S))", name.capitalize(), RUNTIME_UTILS.java, typeName, name)
                }
                field !is OptionalField && !field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.\$L = \$T.<\$T>get(savedInstanceState, \$S)", name, RUNTIME_UTILS.java, typeName, name)
                }
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow()

        typeBuilder.addMethod(injectMethodBuilder.build())
    }
}