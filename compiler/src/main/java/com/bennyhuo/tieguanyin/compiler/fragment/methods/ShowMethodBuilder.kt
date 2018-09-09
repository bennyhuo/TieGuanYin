package com.bennyhuo.tieguanyin.compiler.fragment.methods

import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClassBuilder
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClassBuilder.Companion.METHOD_NAME_FOR_OPTIONALS
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

class ShowMethodBuilder(private val fragmentClass: FragmentClass) {

    fun build(typeBuilder: TypeSpec.Builder) {
        val showMethod = ShowMethod(fragmentClass.type, fragmentClass.sharedElements, FragmentClassBuilder.METHOD_NAME)

        val groupedFields = fragmentClass.fields.groupBy { it is OptionalField }
        val requiredFields = groupedFields[false] ?: emptyList()
        val optionalFields = groupedFields[true] ?: emptyList()

        showMethod.addAllFields(requiredFields)

        val showMethodNoOptional = showMethod.copy(FragmentClassBuilder.METHOD_NAME_NO_OPTIONAL)

        showMethod.addAllFields(optionalFields)

        showMethod.build(typeBuilder)

        //有optional，先来个没有optional的方法
        if (optionalFields.isNotEmpty()) {
            showMethodNoOptional.build(typeBuilder);
        }

        //小于3的情况，只需要每一个参数加一个重载就好
        if (optionalFields.size < 3) {
            optionalFields.forEach { requiredField ->
                showMethodNoOptional.copy(FragmentClassBuilder.METHOD_NAME_FOR_OPTIONAL + requiredField.name.capitalize())
                        .also { it.addField(requiredField) }
                        .build(typeBuilder)
            }
        } else {
            //大于等于3的情况，使用
            val builderName = fragmentClass.simpleName + FragmentClassBuilder.POSIX
            val fillIntentMethodBuilder = MethodSpec.methodBuilder("fillIntent")
                    .addModifiers(PRIVATE)
                    .addParameter(JavaTypes.INTENT, "intent")
            val optionalsClassName = ClassName.get(fragmentClass.packageName, builderName)
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

            showMethodNoOptional.copy(METHOD_NAME_FOR_OPTIONALS)
                    .staticMethod(false)
                    .build(typeBuilder)
        }
    }

}