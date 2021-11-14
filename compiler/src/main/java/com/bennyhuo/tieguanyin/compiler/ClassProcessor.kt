package com.bennyhuo.tieguanyin.compiler

import com.bennyhuo.aptutils.AptContext
import com.bennyhuo.aptutils.logger.Logger
import com.bennyhuo.aptutils.types.isSubTypeOf
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.types.FRAGMENT_CLASS_NAME
import com.bennyhuo.tieguanyin.compiler.basic.types.useAndroidx
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.squareup.kotlinpoet.ClassName
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.system.measureTimeMillis

class ClassProcessor(val filer: Filer) {
    private val activityClasses = HashMap<Element, ActivityClass>()
    private val fragmentClasses = HashMap<Element, FragmentClass>()

    fun process(env: RoundEnvironment) {
        measureTimeMillis {
            checkAndroidx()
            parseClass(env)
            buildFiles()
        }.let {
            AptContext.messager.printMessage(Diagnostic.Kind.NOTE, "TGY-compiler Cost ${it}ms")
        }
    }

    private fun buildFiles() {
        activityClasses.values.map(ActivityClass::builder).forEach { it.build(filer) }
        fragmentClasses.values.map(FragmentClass::builder).forEach { it.build(filer) }
    }

    private fun parseClass(env: RoundEnvironment) {
        env.getElementsAnnotatedWith(Builder::class.java)
            .filterIsInstance<TypeElement>()
            .forEach { element ->
                try {
                    val type = element.asType()
                    when {
                        type.isSubTypeOf("android.app.Activity") -> {
                            activityClasses[element] = ActivityClass.create(element)
                        }
                        type.isSubTypeOf(FRAGMENT_CLASS_NAME) -> {
                            fragmentClasses[element] = FragmentClass.create(element)
                        }
                        else -> {
                            Logger.error(element, "Unsupported type: %s", element.simpleName)
                        }
                    }
                } catch (e: Exception) {
                    Logger.logParsingError(element, Builder::class.java, e)
                    throw e
                }
            }
    }

    private fun checkAndroidx() {
        val androidxVisibleForTesting = ClassName("androidx.annotation", "VisibleForTesting")
        useAndroidx = AptContext.elements.getTypeElement(
            androidxVisibleForTesting.reflectionName()
        ) != null
    }
}