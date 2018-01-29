package com.bennyhuo.activitybuilder

import android.app.Activity
import android.os.Bundle
import com.bennyhuo.annotations.GenerateBuilder
import com.bennyhuo.annotations.Required
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder
class HelloActivity: Activity() {

    @Required("name")
    lateinit var name: String

    @Required("age")
    var age: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        name = intent.extras["name"] as String
        age = intent.extras["age"] as Int
        clickMe.text = name + age
    }
}