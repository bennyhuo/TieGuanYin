package com.bennyhuo.tieguanyin.compiler.fragment.methods

import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicSaveStateMethodBuilder
import com.bennyhuo.tieguanyin.compiler.fragment.FragmentClass
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes

/**
 * Created by benny on 1/31/18.
 */
class SaveStateMethodBuilder(fragmentClass: FragmentClass)
    : BasicSaveStateMethodBuilder(fragmentClass) {

    override val instanceType = JavaTypes.SUPPORT_FRAGMENT

}
