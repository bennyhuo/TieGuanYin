package com.bennyhuo.tieguanyin.compiler

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.entity.Field
import com.bennyhuo.tieguanyin.compiler.basic.entity.OptionalField
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.utils.Logger
import com.bennyhuo.tieguanyin.compiler.utils.isSubTypeOf
import com.google.auto.common.SuperficialValidation
import com.sun.tools.javac.code.Symbol.VarSymbol
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

class ClassProcessor(val filer: Filer){
    private val activityClasses = HashMap<Element, ActivityClass>()
    private val fragmentClasses = HashMap<Element, FragmentClass>()

    fun process(env: RoundEnvironment){
        parseClass(env)
        parseFields(env)
        buildFiles()
    }

    private fun buildFiles() {
        activityClasses.values.map(ActivityClass::builder).forEach { it.build(filer) }
        fragmentClasses.values.map(FragmentClass::builder).forEach { it.build(filer) }
    }

    private fun parseFields(env: RoundEnvironment) {
        env.getElementsAnnotatedWith(Required::class.java)
                .filter { SuperficialValidation.validateElement(it) }
                .filter { it.kind == ElementKind.FIELD }
                .forEach { element ->
                    (activityClasses[element.enclosingElement]
                            ?: fragmentClasses[element.enclosingElement])
                            ?.addSymbol(Field(element as VarSymbol))
                            ?: Logger.error(element, "Field " + element + " annotated as Required while " + element.enclosingElement + " not annotated.")
                }

        env.getElementsAnnotatedWith(Optional::class.java)
                .filter { SuperficialValidation.validateElement(it) }
                .filter { it.kind == ElementKind.FIELD }
                .forEach { element ->
                    (activityClasses[element.enclosingElement]
                            ?: fragmentClasses[element.enclosingElement])
                            ?.addSymbol(OptionalField(element as VarSymbol))
                            ?: Logger.error(element, "Field " + element + " annotated as Optional while " + element.enclosingElement + " not annotated.")
                }
    }

    private fun parseClass(env: RoundEnvironment) {
        env.getElementsAnnotatedWith(Builder::class.java)
                .filter(SuperficialValidation::validateElement)
                .filter { it.kind.isClass }
                .forEach { element ->
                    try {
                        if (element.asType().isSubTypeOf("android.app.Activity")) {
                            activityClasses[element] = ActivityClass(element as TypeElement)
                        } else if (element.asType().isSubTypeOf("android.support.v4.app.Fragment")) {
                            fragmentClasses[element] = FragmentClass(element as TypeElement)
                        } else {
                            Logger.error(element, "Unsupported type: %s", element.simpleName)
                        }
                    } catch (e: Exception) {
                        Logger.logParsingError(element, Builder::class.java, e)
                    }
                }
        activityClasses.values.forEach { it.setUpSuperClass(activityClasses) }
    }
}