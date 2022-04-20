package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClassBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.BasicConstantBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.utils.camelToUnderline
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.*

class ConstantBuilder(private val activityClass: ActivityClass)
    : BasicConstantBuilder(activityClass) {

    override fun build(typeBuilder: TypeSpec.Builder) {
        super.build(typeBuilder)
        activityClass.resultParameters.forEach { resultEntity ->
            typeBuilder.addProperty(
                PropertySpec.builder(
                    ActivityClassBuilder.CONSTS_RESULT_PREFIX + resultEntity.name.camelToUnderline().uppercase(Locale.getDefault()),
                    String::class, KModifier.CONST
                ).initializer("%S", resultEntity.name).build()
            )
        }
    }
}