package com.bennyhuo.tieguanyin.sample.inherited

import android.os.Bundle
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.sample.common.BaseActivity

@Builder
class SubActivity: AbsActivity() {

    @Required
    var age: Int = 0

//    @Required
//    var age2: Int = 0
//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("He")
    }

}