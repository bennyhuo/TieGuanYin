package com.bennyhuo.tieguanyin.compiler.ksp.activity.builder

import com.bennyhuo.tieguanyin.compiler.ksp.activity.ActivityClass
import com.bennyhuo.tieguanyin.compiler.ksp.basic.builder.BasicInjectMethodBuilder
import com.bennyhuo.tieguanyin.compiler.ksp.basic.types.ACTIVITY

/**
 * Created by benny on 1/31/18.
 */

class InjectMethodBuilder(activityClass: ActivityClass): BasicInjectMethodBuilder(activityClass) {

    override val instanceType = ACTIVITY.java

    override val snippetToRetrieveState = "typedInstance.getIntent().getExtras() : savedInstanceState"

}
