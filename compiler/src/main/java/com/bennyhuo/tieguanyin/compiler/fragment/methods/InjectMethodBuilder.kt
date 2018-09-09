package com.bennyhuo.tieguanyin.compiler.fragment.methods

import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicInjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes

/**
 * Created by benny on 1/31/18.
 */

class InjectMethodBuilder(private val fragmentClass: FragmentClass): BasicInjectMethodBuilder(fragmentClass) {

    override val instanceType = JavaTypes.SUPPORT_FRAGMENT

    override val snippetToRetrieveState = "typedInstance.getArguments() : savedInstanceState"

}
