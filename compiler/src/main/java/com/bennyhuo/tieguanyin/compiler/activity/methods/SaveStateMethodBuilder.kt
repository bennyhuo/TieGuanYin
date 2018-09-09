package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicSaveStateMethodBuilder
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes

/**
 * Created by benny on 1/31/18.
 */
class SaveStateMethodBuilder(activityClass: ActivityClass)
    : BasicSaveStateMethodBuilder(activityClass) {

    override val instanceType = JavaTypes.ACTIVITY

}
