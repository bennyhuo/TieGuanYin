package com.bennyhuo.tieguanyin.compiler.basic.builder

import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes
import com.bennyhuo.tieguanyin.compiler.utils.Utils
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

abstract class BasicSaveStateMethodBuilder(val basicClass: BasicClass) {

    abstract val instanceType: TypeName

    fun build(typeBuilder: TypeSpec.Builder) {
        val methodBuilder = MethodSpec.methodBuilder("saveState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(instanceType, "instance")
                .addParameter(JavaTypes.BUNDLE, "outState")
                .beginControlFlow("if(instance instanceof \$T)", basicClass.type)
                .addStatement("\$T typedInstance = (\$T) instance", basicClass.type, basicClass.type)

        methodBuilder.addStatement("\$T intent = new \$T()", JavaTypes.INTENT, JavaTypes.INTENT)

        for (requiredField in basicClass.fields) {
            val name = requiredField.name
            if (requiredField.isPrivate) {
                methodBuilder.addStatement("intent.putExtra(\$S, typedInstance.get\$L())", name, Utils.capitalize(name))
            } else {
                methodBuilder.addStatement("intent.putExtra(\$S, typedInstance.\$L)", name, name)
            }
        }

        methodBuilder.addStatement("outState.putAll(intent.getExtras())").endControlFlow()

        typeBuilder.addMethod(methodBuilder.build())
    }

}