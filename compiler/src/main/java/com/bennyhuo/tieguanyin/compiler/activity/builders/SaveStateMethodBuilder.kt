package com.bennyhuo.tieguanyin.compiler.activity.builders

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicSaveStateMethodBuilder
import com.bennyhuo.tieguanyin.compiler.basic.types.ACTIVITY

/**
 * Created by benny on 1/31/18.
 */
class SaveStateMethodBuilder(activityClass: ActivityClass)
    : BasicSaveStateMethodBuilder(activityClass) {

    override val instanceType = ACTIVITY.java

}
