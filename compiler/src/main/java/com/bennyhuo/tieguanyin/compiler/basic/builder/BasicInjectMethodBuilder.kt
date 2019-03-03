package com.bennyhuo.tieguanyin.compiler.basic.builder

import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.basic.types.BUNDLE
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
                .beginControlFlow("if(instance instanceof \$T)", basicClass.typeElement)
                .addStatement("\$T typedInstance = (\$T) instance", basicClass.typeElement, basicClass.typeElement)
                .beginControlFlow("if(savedInstanceState != null)")

        for (field in basicClass.fields) {
            val name = field.name
            val template = field.javaTemplateFromBundle("savedInstanceState")
            if (field.isPrivate) {
                injectMethodBuilder.addStatement("typedInstance.set\$L(${template.first})", name.capitalize(), *template.second)
            } else {
                injectMethodBuilder.addStatement("typedInstance.\$L = ${template.first}", name, *template.second)
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow()

        typeBuilder.addMethod(injectMethodBuilder.build())
    }
}