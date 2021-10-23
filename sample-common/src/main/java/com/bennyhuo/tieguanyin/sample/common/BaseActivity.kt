package com.bennyhuo.tieguanyin.sample.common

import androidx.appcompat.app.AppCompatActivity
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required

@Builder
open class BaseActivity: AppCompatActivity() {

    @Required
    var id: Long = 0

    @Optional
    var name: String? = null

}