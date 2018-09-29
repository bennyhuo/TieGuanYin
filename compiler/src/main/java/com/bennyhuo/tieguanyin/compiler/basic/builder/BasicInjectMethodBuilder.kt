package com.bennyhuo.tieguanyin.compiler.basic.builder

import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.basic.types.BUNDLE
import com.bennyhuo.tieguanyin.compiler.basic.types.RUNTIME_UTILS
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
                .beginControlFlow("if(instance instanceof \$T)", basicClass.type)
                .addStatement("\$T typedInstance = (\$T) instance", basicClass.type, basicClass.type)
                .addStatement("\$T extras = savedInstanceState == null ? $snippetToRetrieveState", BUNDLE.java)
                .beginControlFlow("if(extras != null)")

        for (field in basicClass.fields) {
            val name = field.name
            val typeName = field.asTypeName().box()

            when {
                field is OptionalField && field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.set\$L(\$T.<\$T>get(extras, \$S, \$L))", name.capitalize(), RUNTIME_UTILS.java, typeName, name, field.defaultValue)
                }
                field is OptionalField && !field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.\$L = \$T.<\$T>get(extras, \$S, \$L)", name, RUNTIME_UTILS.java, typeName, name, field.defaultValue)
                }
                field !is OptionalField && field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.set\$L(\$T.<\$T>get(extras, \$S))", name.capitalize(), RUNTIME_UTILS.java, typeName, name)
                }
                field !is OptionalField && !field.isPrivate -> {
                    injectMethodBuilder.addStatement("typedInstance.\$L = \$T.<\$T>get(extras, \$S)", name, RUNTIME_UTILS.java, typeName, name)
                }
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow()

        typeBuilder.addMethod(injectMethodBuilder.build())
    }
}