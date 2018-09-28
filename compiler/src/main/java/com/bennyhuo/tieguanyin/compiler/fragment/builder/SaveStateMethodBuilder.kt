package com.bennyhuo.tieguanyin.compiler.fragment.builder

import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicSaveStateMethodBuilder
import com.bennyhuo.tieguanyin.compiler.basic.types.SUPPORT_FRAGMENT
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass

/**
 * Created by benny on 1/31/18.
 */
class SaveStateMethodBuilder(fragmentClass: FragmentClass)
    : BasicSaveStateMethodBuilder(fragmentClass) {

    override val instanceType = SUPPORT_FRAGMENT.java

}
