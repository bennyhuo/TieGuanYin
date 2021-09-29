package com.bennyhuo.tieguanyin.compiler

import com.bennyhuo.aptutils.AptContext
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * Created by benny on 10/2/16.
 *  注解处理器入口，注意google 的 auto-service 不支持
 */
class BuilderProcessor : AbstractProcessor() {

    private val supportedAnnotations = setOf(Builder::class.java, Required::class.java, Optional::class.java)

    @Synchronized
    override fun init(env: ProcessingEnvironment) {
        super.init(env)
        AptContext.init(env)
    }

    override fun getSupportedAnnotationTypes() = supportedAnnotations.mapTo(HashSet<String>(), Class<*>::getCanonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_8

    override fun process(annotations: Set<TypeElement>, env: RoundEnvironment): Boolean {
        ClassProcessor(AptContext.filer).process(env)
        return true
    }

}
