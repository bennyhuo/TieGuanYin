package com.bennyhuo.tieguanyin.compiler.activity.builder

import com.bennyhuo.tieguanyin.compiler.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.basic.builder.BasicInjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.basic.types.ACTIVITY

/**
 * Created by benny on 1/31/18.
 */

class InjectMethodBuilder(activityClass: ActivityClass): BasicInjectMethodBuilder(activityClass) {

    override val instanceType = ACTIVITY.java

    override val snippetToRetrieveState = "typedInstance.getIntent().getExtras() : savedInstanceState"

}
