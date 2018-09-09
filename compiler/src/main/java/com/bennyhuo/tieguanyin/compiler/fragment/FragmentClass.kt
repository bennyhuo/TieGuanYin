package com.bennyhuo.tieguanyin.compiler.fragment

import com.bennyhuo.tieguanyin.compiler.basic.BasicClass
import javax.lang.model.element.TypeElement

/**
 * Created by benny on 1/29/18.
 */
class FragmentClass(type: TypeElement): BasicClass(type){
    val builder = FragmentClassBuilder(this)
}
