package com.bennyhuo.tieguanyin.compiler.ksp

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT_CLASS_NAME
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.useAndroidx
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.ksp.utils.asType
import com.bennyhuo.tieguanyin.compiler.ksp.utils.isSubTypeOf
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import java.util.*
import kotlin.system.measureNanoTime

class ClassProcessor {
    private val activityClasses = HashMap<KSNode, ActivityClass>()
    private val fragmentClasses = HashMap<KSNode, FragmentClass>()

    fun process(resolver: Resolver){
        measureNanoTime {
            checkAndroidx(resolver)
            parseClass(resolver)
            buildFiles()
        }.let {
            logger.warn("Cost time: ${it}ns")
        }
    }

    private fun buildFiles() {
        activityClasses.values.map(ActivityClass::builder).forEach { it.build() }
        fragmentClasses.values.map(FragmentClass::builder).forEach { it.build() }
    }

    private fun parseClass(resolver: Resolver) {
        resolver.getSymbolsWithAnnotation(Builder::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .forEach { declaration ->
                    try {
                        val type = declaration.asStarProjectedType()
                        when {
                            type.isSubTypeOf("android.app.Activity") -> {
                                activityClasses[declaration] = ActivityClass.create(declaration)
                            }
                            type.isSubTypeOf(FRAGMENT_CLASS_NAME) -> {
                                fragmentClasses[declaration] = FragmentClass.create(declaration)
                            }
                            else -> {
                                logger.error("Unsupported type: %s", declaration)
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e.toString(), declaration)
                        throw e
                    }
                }
    }

    private fun checkAndroidx(resolver: Resolver) {
        val androidxVisibleForTesting = ClassName("androidx.annotation", "VisibleForTesting")
        useAndroidx = resolver.getClassDeclarationByName(
            androidxVisibleForTesting.reflectionName()) != null
    }
}