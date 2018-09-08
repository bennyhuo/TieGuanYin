package com.bennyhuo.tieguanyin.compiler

import com.bennyhuo.tieguanyin.annotations.ActivityBuilder
import com.bennyhuo.tieguanyin.annotations.FragmentBuilder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.OptionalField
import com.bennyhuo.tieguanyin.compiler.basic.RequiredField
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.utils.Logger
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils
import com.google.auto.common.SuperficialValidation
import com.sun.tools.javac.code.Symbol
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import kotlin.collections.HashSet

/**
 * Created by benny on 10/2/16.
 *  注解处理器入口，注意google 的 auto-service 不支持
 */
class BuilderProcessor : AbstractProcessor() {

    private lateinit var filer: Filer

    private val supportedAnnotations = setOf(ActivityBuilder::class.java, FragmentBuilder::class.java,
            Required::class.java, Optional::class.java)

    @Synchronized
    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        filer = env.filer

        TypeUtils.types = env.typeUtils
        Logger.messager = env.messager
    }

    override fun getSupportedAnnotationTypes() = supportedAnnotations.mapTo(HashSet<String>(), Class<*>::getCanonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_7

    override fun process(annotations: Set<TypeElement>, env: RoundEnvironment): Boolean {
        val activityClasses = HashMap<Element, ActivityClass>()
        val fragmentClasses = HashMap<Element, FragmentClass>()
        parseActivityClass(env, activityClasses)
        parseFragmentClass(env, fragmentClasses)
        parseFields(env, activityClasses, fragmentClasses)
        brewFiles(activityClasses, fragmentClasses)
        return true
    }

    private fun brewFiles(activityClasses: HashMap<Element, ActivityClass>, fragmentClasses: HashMap<Element, FragmentClass>) {
        activityClasses.values.map(ActivityClass::builder).forEach { it.build(filer) }
        fragmentClasses.values.forEach { it.brew(filer) }
    }

    private fun parseFields(env: RoundEnvironment, activityClasses: HashMap<Element, ActivityClass>, fragmentClasses: HashMap<Element, FragmentClass>) {
        for (element in env.getElementsAnnotatedWith(Required::class.java)) {
            if (!SuperficialValidation.validateElement(element)) continue
            try {
                if (element.kind == ElementKind.FIELD) {
                    val activityClass = activityClasses[element.enclosingElement]
                    if (activityClass == null) {
                        val fragmentClass = fragmentClasses[element.enclosingElement]
                        if (fragmentClass == null) {
                            Logger.error(element, "Field " + element + " annotated as Required while " + element.enclosingElement + " not annotated.")
                        } else {
                            fragmentClass.addSymbol(RequiredField(element as Symbol.VarSymbol))
                        }
                    } else {
                        activityClass.addSymbol(RequiredField(element as Symbol.VarSymbol))
                    }
                }
            } catch (e: Exception) {
                Logger.logParsingError(element, Required::class.java, e)
            }

        }

        for (element in env.getElementsAnnotatedWith(Optional::class.java)) {
            if (!SuperficialValidation.validateElement(element)) continue
            try {
                if (element.kind == ElementKind.FIELD) {
                    val activityClass = activityClasses[element.enclosingElement]
                    if (activityClass == null) {
                        val fragmentClass = fragmentClasses[element.enclosingElement]
                        if (fragmentClass == null) {
                            Logger.error(element, "Field " + element + " annotated as Optional while " + element.enclosingElement + " not annotated.")
                        } else {
                            fragmentClass.addSymbol(OptionalField(element as Symbol.VarSymbol))
                        }
                    } else {
                        activityClass.addSymbol(OptionalField(element as Symbol.VarSymbol))
                    }
                }
            } catch (e: Exception) {
                Logger.logParsingError(element, Required::class.java, e)
            }
        }
    }

    private fun parseActivityClass(env: RoundEnvironment, activityClasses: HashMap<Element, ActivityClass>) {
        for (element in env.getElementsAnnotatedWith(ActivityBuilder::class.java)) {
            if (!SuperficialValidation.validateElement(element)) continue
            try {
                if (element.kind.isClass) {
                    activityClasses[element] = ActivityClass(element as TypeElement)
                }
            } catch (e: Exception) {
                Logger.logParsingError(element, ActivityBuilder::class.java, e)
            }
        }

        activityClasses.values.forEach { it.setupSuperClass(activityClasses) }
    }

    private fun parseFragmentClass(env: RoundEnvironment, fragmentClasses: HashMap<Element, FragmentClass>) {
        for (element in env.getElementsAnnotatedWith(FragmentBuilder::class.java)) {
            if (!SuperficialValidation.validateElement(element)) continue
            try {
                if (element.kind.isClass) {
                    fragmentClasses[element] = FragmentClass(element as TypeElement)
                }
            } catch (e: Exception) {
                Logger.logParsingError(element, FragmentBuilder::class.java, e)
            }
        }

        fragmentClasses.values.forEach{ it.setupSuperClass(fragmentClasses) }
    }
}
