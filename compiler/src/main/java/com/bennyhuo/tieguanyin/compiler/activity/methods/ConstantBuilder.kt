package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicConstantBuilder
import com.bennyhuo.tieguanyin.compiler.utils.Utils
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class ConstantBuilder(private val activityClass: ActivityClass)
    : BasicConstantBuilder(activityClass) {

    override fun build(typeBuilder: TypeSpec.Builder) {
        super.build(typeBuilder)
        activityClass.activityResultClass?.resultParameters?.forEach { resultEntity ->
            typeBuilder.addField(FieldSpec.builder(String::class.java,
                    ActivityClassBuilder.CONSTS_RESULT_PREFIX + Utils.camelToUnderline(resultEntity.name),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", resultEntity.name)
                    .build())
        }
    }
}