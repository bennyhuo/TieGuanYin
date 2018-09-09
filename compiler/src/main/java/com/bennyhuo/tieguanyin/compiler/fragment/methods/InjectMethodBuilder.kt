package com.bennyhuo.tieguanyin.compiler.fragment.methods

import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicInjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.basic.types.SUPPORT_FRAGMENT
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass

/**
 * Created by benny on 1/31/18.
 */

class InjectMethodBuilder(private val fragmentClass: FragmentClass): BasicInjectMethodBuilder(fragmentClass) {

    override val instanceType = SUPPORT_FRAGMENT.java

    override val snippetToRetrieveState = "typedInstance.getArguments() : savedInstanceState"

}
