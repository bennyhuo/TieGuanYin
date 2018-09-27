package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.basic.types.INTENT
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

class StartMethodBuilder(private val activityClass: ActivityClass) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val startMethod = StartMethod(activityClass, ActivityClassBuilder.METHOD_NAME)

        val groupedFields = activityClass.fields.groupBy { it is OptionalField }
        val requiredFields = groupedFields[false] ?: emptyList()
        val optionalFields = groupedFields[true] ?: emptyList()

        startMethod.addAllFields(requiredFields)

        val startMethodNoOptional = startMethod.copy(ActivityClassBuilder.METHOD_NAME_NO_OPTIONAL)

        startMethod.addAllFields(optionalFields)

        startMethod.build(typeBuilder)

        //有optional，先来个没有optional的方法
        if (optionalFields.isNotEmpty()) {
            startMethodNoOptional.build(typeBuilder);
        }

        //小于3的情况，只需要每一个参数加一个重载就好
        if (optionalFields.size < 3) {
            optionalFields.forEach { requiredField ->
                startMethodNoOptional.copy(ActivityClassBuilder.METHOD_NAME_FOR_OPTIONAL + requiredField.name.capitalize())
                        .also { it.addField(requiredField) }
                        .build(typeBuilder)
            }
        } else {
            //大于等于3的情况，使用
            val fillIntentMethodBuilder = MethodSpec.methodBuilder("fillIntent")
                    .addModifiers(PRIVATE)
                    .addParameter(INTENT.java, "intent")
            val optionalsClassName = ClassName.get(activityClass.packageName, activityClass.builderClassName)
            optionalFields.forEach { requiredField ->
                typeBuilder.addField(FieldSpec.builder(requiredField.asTypeName(), requiredField.name, PRIVATE).build())
                typeBuilder.addMethod(MethodSpec.methodBuilder(requiredField.name)
                        .addModifiers(PUBLIC)
                        .addParameter(requiredField.asTypeName(), requiredField.name)
                        .addStatement("this.${requiredField.name} = ${requiredField.name}")
                        .addStatement("return this")
                        .returns(optionalsClassName)
                        .build())
                if (requiredField.isPrimitive) {
                    fillIntentMethodBuilder.addStatement("intent.putExtra(\$S, \$L)", requiredField.name, requiredField.name)
                } else {
                    fillIntentMethodBuilder
                            .beginControlFlow("if(\$L != null)", requiredField.name)
                            .addStatement("intent.putExtra(\$S, \$L)", requiredField.name, requiredField.name)
                            .endControlFlow()
                }
            }
            typeBuilder.addMethod(fillIntentMethodBuilder.build())

            startMethodNoOptional.copy(ActivityClassBuilder.METHOD_NAME_FOR_OPTIONALS)
                    .staticMethod(false)
                    .build(typeBuilder)
        }
    }

}