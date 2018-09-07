package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.OptionalField
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

object MethodsBuilder {

    fun buildStartMethod(activityClass: ActivityClass, typeBuilder: TypeSpec.Builder) {
        val startMethod = StartMethod(activityClass, ActivityClass.METHOD_NAME)

        val groupedFields = activityClass.requiredFieldsRecursively.groupBy { it is OptionalField }
        val requiredFields = groupedFields[false] ?: emptyList()
        val optionalFields = groupedFields[true] ?: emptyList()
        requiredFields.forEach(startMethod::visitField)

        val startMethodNoOptional = startMethod.copy(ActivityClass.METHOD_NAME_NO_OPTIONAL)

        optionalFields.forEach(startMethod::visitField)

        startMethod.brew(typeBuilder)

        //有optional，先来个没有optional的方法
        if(optionalFields.isNotEmpty()){
            startMethodNoOptional.brew(typeBuilder);
        }

        //小于3的情况，只需要每一个参数加一个重载就好
        if(optionalFields.size < 3){
            optionalFields.forEach { requiredField ->
                startMethodNoOptional.copy(ActivityClass.METHOD_NAME_FOR_OPTIONAL + requiredField.name.capitalize())
                        .also { it.visitField(requiredField) }
                        .brew(typeBuilder)
            }
        } else {
            //大于等于3的情况，使用
            val builderName = activityClass.simpleName + ActivityClass.POSIX
            val fillIntentMethodBuilder = MethodSpec.methodBuilder("fillIntent")
                    .addModifiers(PRIVATE)
                    .addParameter(JavaTypes.INTENT, "intent")
            val optionalsClassName = ClassName.get(activityClass.packageName, builderName)
            optionalFields.forEach {
                requiredField ->
                typeBuilder.addField(FieldSpec.builder(ClassName.get(requiredField.symbol.type), requiredField.name, PRIVATE).build())
                typeBuilder.addMethod(MethodSpec.methodBuilder(requiredField.name)
                        .addModifiers(PUBLIC)
                        .addParameter(ClassName.get(requiredField.symbol.type), requiredField.name)
                        .addStatement("this.${requiredField.name} = ${requiredField.name}")
                        .addStatement("return this")
                        .returns(optionalsClassName)
                        .build())
                if(requiredField.symbol.type.isPrimitive){
                    fillIntentMethodBuilder.addStatement("intent.putExtra(\$S, \$L)", requiredField.name, requiredField.name)
                } else {
                    fillIntentMethodBuilder
                            .beginControlFlow("if(\$L != null)", requiredField.name)
                            .addStatement("intent.putExtra(\$S, \$L)", requiredField.name, requiredField.name)
                            .endControlFlow()
                }
            }
            typeBuilder.addMethod(fillIntentMethodBuilder.build())

            startMethodNoOptional.copy("startWithOptionals")
                    .staticMethod(false)
                    .brew(typeBuilder)
        }
    }
}
