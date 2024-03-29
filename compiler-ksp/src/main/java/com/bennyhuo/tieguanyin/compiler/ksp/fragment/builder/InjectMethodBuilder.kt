package com.bennyhuo.tieguanyin.compiler.ksp.fragment.builder

import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.BasicInjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.FRAGMENT
import com.bennyhuo.tieguanyin.compiler.ksp.fragment.FragmentClass

/**
 * Created by benny on 1/31/18.
 */

class InjectMethodBuilder(fragmentClass: FragmentClass): BasicInjectMethodBuilder(fragmentClass) {

    override val instanceType = FRAGMENT.kotlin

}
