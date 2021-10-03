package com.bennyhuo.tieguanyin.compiler.ksp

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.ksp.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.useAndroidx
import com.bennyhuo.tieguanyin.compiler.ksp.core.logger
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.ksp.utils.asType
import com.bennyhuo.tieguanyin.compiler.ksp.utils.isSubTypeOf
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import java.util.*
import kotlin.system.measureNanoTime

class ClassProcessor {
    private val activityClasses = HashMap<KSNode, ActivityClass>()
    private val fragmentClasses = HashMap<KSNode, FragmentClass>()

    fun process(resolver: Resolver){
        measureNanoTime {
            parseClass(resolver)
            parseFields(resolver)
            buildFiles()
        }.let {
            logger.warn("Cost time: ${it}ns")
        }
    }

    private fun buildFiles() {
        activityClasses.values.map(ActivityClass::builder).forEach { it.build() }
        fragmentClasses.values.map(FragmentClass::builder).forEach { it.build() }
    }

    private fun parseFields(resolver: Resolver) {
        resolver.getSymbolsWithAnnotation(Required::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()
            .filter { it.parent != null }
            .forEach { element ->
                val parent = element.parent!!
                    (activityClasses[parent]
                            ?: fragmentClasses[parent])
                            ?.addSymbol(Field(element))
                            ?: logger.error("Field $element annotated as Required while $parent not annotated.", element)
                }

        resolver.getSymbolsWithAnnotation(Optional::class.qualifiedName!!)
            .filterIsInstance<KSPropertyDeclaration>()
            .filter { it.parent != null }
            .forEach { element ->
                val parent = element.parent!!
                (activityClasses[parent]
                    ?: fragmentClasses[parent])
                    ?.addSymbol(OptionalField(element))
                    ?: logger.error("Field $element annotated as Optional while $parent not annotated.", element)
            }
    }

    private fun parseClass(resolver: Resolver) {
        resolver.getSymbolsWithAnnotation(Builder::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .forEach { element ->
                    try {
                        if (element.asType().isSubTypeOf("android.app.Activity")) {
                            activityClasses[element] = ActivityClass(element)
                        } else if (element.asType().isSubTypeOf("android.support.v4.app.Fragment")) {
                            useAndroidx = false
                            fragmentClasses[element] = FragmentClass(element)
                        } else if (element.asType().isSubTypeOf("androidx.fragment.app.Fragment")) {
                            useAndroidx = true
                            fragmentClasses[element] = FragmentClass(element)
                        } else {
                            logger.error("Unsupported type: %s", element)
                        }
                    } catch (e: Exception) {
                        logger.error(e.toString(), element)
                        throw e
                    }
                }
        activityClasses.values.forEach { it.setUpSuperClass(activityClasses) }
    }
}