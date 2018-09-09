package com.bennyhuo.tieguanyin.compiler

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.compiler.utils.Logger
import com.bennyhuo.tieguanyin.compiler.utils.TypeUtils
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * Created by benny on 10/2/16.
 *  注解处理器入口，注意google 的 auto-service 不支持
 */
class BuilderProcessor : AbstractProcessor() {

    private lateinit var filer: Filer

    private val supportedAnnotations = setOf(Builder::class.java, Required::class.java, Optional::class.java)

    @Synchronized
    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        filer = env.filer
        TypeUtils.elements = env.elementUtils
        TypeUtils.types = env.typeUtils
        Logger.messager = env.messager
    }

    override fun getSupportedAnnotationTypes() = supportedAnnotations.mapTo(HashSet<String>(), Class<*>::getCanonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_7

    override fun process(annotations: Set<TypeElement>, env: RoundEnvironment): Boolean {
        ClassProcessor(filer).process(env)
        return true
    }

}
