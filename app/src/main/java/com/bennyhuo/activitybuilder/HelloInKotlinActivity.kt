package com.bennyhuo.activitybuilder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.bennyhuo.annotations.GenerateBuilder
import com.bennyhuo.annotations.Optional
import com.bennyhuo.annotations.Required
import com.bennyhuo.annotations.ResultEntity

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder(forResult = true, resultTypes = [(ResultEntity(name = "java", type = String::class)), (ResultEntity(name = "kotlin", type = Int::class))])
class HelloInKotlinActivity : AppCompatActivity() {

    @Required
    var num: Int = 0

    @Optional
    var java: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        val textView = findViewById<TextView>(R.id.clickMe)
        textView.setOnClickListener {
            val testIntent = Intent()
            testIntent.putExtra("kotlin", 1234567)
            testIntent.putExtra("java", "I am Java!")
            setResult(Activity.RESULT_OK, testIntent)
            finish()
        }
    }
}
