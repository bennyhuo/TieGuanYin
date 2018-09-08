package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.annotations.ResultEntity
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.utils.Utils
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class ConstantBuilder(private val activityClass: ActivityClass) {
    fun build(typeBuilder: TypeSpec.Builder) {
        activityClass.requiredFieldsRecursively.forEach { field ->
            typeBuilder.addField(FieldSpec.builder(String::class.java,
                    field.prefix + Utils.camelToUnderline(field.name),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", field.name)
                    .build())
        }

        activityClass.activityResultClass?.resultEntitiesRecursively?.forEach { resultEntity: ResultEntity ->
            typeBuilder.addField(FieldSpec.builder(String::class.java,
                    ActivityClassBuilder.CONSTS_RESULT_PREFIX + Utils.camelToUnderline(resultEntity.name),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("\$S", resultEntity.name)
                    .build())
        }
    }
}