package com.bennyhuo.activitybuilder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.activitybuilder.runtime.annotations.GenerateBuilder
import com.bennyhuo.activitybuilder.runtime.annotations.GenerateMode.KotlinOnly
import com.bennyhuo.activitybuilder.runtime.annotations.Optional
import com.bennyhuo.activitybuilder.runtime.annotations.Required
import com.bennyhuo.activitybuilder.runtime.factory.ObjectCreator
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder(mode = KotlinOnly)
class HelloActivity: AppCompatActivity() {

    @Required()
    lateinit var name: String

    @Required()
    var age: Int = 0

    @Optional(intValue = 123)
    var num: Int = 0

    @Optional(stringValue = "I'm title!!")
    lateinit var title: String

    @Optional()
    lateinit var subTitle: String

    @Optional(creator = DetailsCreator::class)
    lateinit var details: Serializable

    class DetailsCreator: ObjectCreator<Serializable>{
        override fun create(cls: Class<Serializable>):Serializable {
            return "details..."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        name = intent.extras["name"] as String
//        age = intent.extras["age"] as Int
//        title = intent.extras["title"] as String? ?: "No Title"
        setTitle(title)
        clickMe.text = name + age + num + details
    }

    override fun finish() {
        super.finish()
    }

    companion object {

    }
}