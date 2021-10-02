package com.bennyhuo.tieguanyin.compiler.ksp

import com.bennyhuo.tieguanyin.compiler.ksp.core.KspContext
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Created by benny at 2021/6/20 19:03.
 */
class BuilderSymbolProcessorProvider: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return BuilderSymbolProcessor(environment)
    }
}

class BuilderSymbolProcessor(private val environment: SymbolProcessorEnvironment): SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        KspContext.environment = environment
        KspContext.resolver = resolver

        ClassProcessor().process(resolver)

        return emptyList()
    }

}