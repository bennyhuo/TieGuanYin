package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.BasicSaveStateMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY

/**
 * Created by benny on 1/31/18.
 */
class SaveStateMethodBuilder(activityClass: ActivityClass)
    : BasicSaveStateMethodBuilder(activityClass) {

    override val instanceType = ACTIVITY.java

}
