package com.bennyhuo.tieguanyin.compiler.basic.builder

import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes
import com.bennyhuo.tieguanyin.compiler.utils.Utils
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
                .addParameter(JavaTypes.BUNDLE, "savedInstanceState")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .beginControlFlow("if(instance instanceof \$T)", basicClass.type)
                .addStatement("\$T typedInstance = (\$T) instance", basicClass.type, basicClass.type)
                .addStatement("\$T extras = savedInstanceState == null ? $snippetToRetrieveState", JavaTypes.BUNDLE)
                .beginControlFlow("if(extras != null)")

        for (requiredField in basicClass.fields) {
            val name = requiredField.name
            val typeName = requiredField.asTypeName().box()

            if (requiredField is OptionalField) {
                injectMethodBuilder.addStatement("\$T \$LValue = \$T.<\$T>get(extras, \$S, \$L)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name, requiredField.defaultValue)
            } else {
                injectMethodBuilder.addStatement("\$T \$LValue = \$T.<\$T>get(extras, \$S)", typeName, name, JavaTypes.RUNTIME_UTILS, typeName, name)
            }
            if (requiredField.isPrivate) {
                injectMethodBuilder.addStatement("typedInstance.set\$L(\$LValue)", Utils.capitalize(name), name)
            } else {
                injectMethodBuilder.addStatement("typedInstance.\$L = \$LValue", name, name)
            }
        }
        injectMethodBuilder.endControlFlow().endControlFlow()

        typeBuilder.addMethod(injectMethodBuilder.build())
    }
}