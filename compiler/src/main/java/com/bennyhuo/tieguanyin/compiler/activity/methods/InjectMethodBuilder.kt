package com.bennyhuo.tieguanyin.compiler.activity.methods

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicInjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.utils.JavaTypes

/**
 * Created by benny on 1/31/18.
 */

class InjectMethodBuilder(activityClass: ActivityClass): BasicInjectMethodBuilder(activityClass) {

    override val instanceType = JavaTypes.ACTIVITY

    override val snippetToRetrieveState = "typedInstance.getIntent().getExtras() : savedInstanceState"

}
