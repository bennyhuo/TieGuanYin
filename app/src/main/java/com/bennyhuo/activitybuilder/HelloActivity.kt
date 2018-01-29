package com.bennyhuo.activitybuilder

import android.app.Activity
import com.bennyhuo.annotations.GenerateBuilder
import com.bennyhuo.annotations.Required

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder
class HelloActivity: Activity() {

    @Required("name")
    lateinit var name: String

    @Required("age")
    var age: Int = 0

}