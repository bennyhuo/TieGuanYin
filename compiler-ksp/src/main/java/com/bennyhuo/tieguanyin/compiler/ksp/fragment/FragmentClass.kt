package com.bennyhuo.tieguanyin.compiler.ksp.fragment

import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.bennyhuo.tieguanyin.compiler.ksp.utils.getFirstAnnotationByType
import com.bennyhuo.tieguanyin.compiler.ksp.utils.getFirstAnnotationByTypeOrNull
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.util.*

/**
 * Created by benny on 1/29/18.
 */
class FragmentClass
private constructor(type: KSClassDeclaration, builder: Builder) : BasicClass(type, builder) {

    companion object {
        private val fragmentClassCache = WeakHashMap<KSClassDeclaration, FragmentClass>()

        fun getOrNull(declaration: KSClassDeclaration): FragmentClass? {
            var fragmentClass = fragmentClassCache[declaration]
            if (fragmentClass == null) {
                val builder = declaration.getFirstAnnotationByTypeOrNull(Builder::class) ?: return null
                fragmentClass = FragmentClass(declaration, builder)
                fragmentClassCache[declaration] = fragmentClass
            }
            return fragmentClass
        }

        fun create(declaration: KSClassDeclaration): FragmentClass {
            return fragmentClassCache.getOrPut(declaration){
                FragmentClass(declaration, declaration.getFirstAnnotationByType(Builder::class))
            }
        }
    }


    val builder = FragmentClassBuilder(this)

    override fun createSuperClass(superClassDeclaration: KSClassDeclaration) = getOrNull(superClassDeclaration)
}
