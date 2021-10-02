package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.BasicConstantBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.utils.camelToUnderline
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class ConstantBuilder(private val activityClass: ActivityClass)
    : BasicConstantBuilder(activityClass) {

    override fun build(typeBuilder: TypeSpec.Builder) {
        super.build(typeBuilder)
        activityClass.resultParameters.forEach { resultEntity ->
            typeBuilder.addField(FieldSpec.builder(String::class.java,
                    ActivityClassBuilder.CONSTS_RESULT_PREFIX + resultEntity.name.camelToUnderline(),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", resultEntity.name)
                    .build())
        }
    }
}