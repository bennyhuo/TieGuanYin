package com.bennyhuo.activitybuilder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.annotations.GenerateBuilder
import com.bennyhuo.annotations.Optional
import com.bennyhuo.annotations.Required
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder
class HelloActivity: AppCompatActivity() {

    @Required("name")
    lateinit var name: String

    @Required("age")
    var age: Int = 0

    @Optional("title")
    lateinit var title: String

    @Optional("subTitle")
    lateinit var subTitle: String

    @Optional("details")
    lateinit var details: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        name = intent.extras["name"] as String
        age = intent.extras["age"] as Int
        title = intent.extras["title"] as String? ?: "No Title"
        setTitle(title)
        clickMe.text = name + age
    }
}