package com.bennyhuo.tieguanyin.compiler.ksp.fragment

import com.bennyhuo.tieguanyin.compiler.ksp.basic.BasicClass
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Created by benny on 1/29/18.
 */
class FragmentClass(type: KSClassDeclaration): BasicClass(type){
    val builder = FragmentClassBuilder(this)
}
