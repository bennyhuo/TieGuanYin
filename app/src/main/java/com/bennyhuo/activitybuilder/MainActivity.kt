package com.bennyhuo.activitybuilder

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.bennyhuo.activitybuilder.utils.startJavaActivity
import com.bennyhuo.activitybuilder.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var x = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(this.javaClass.simpleName)

        val button = findViewById<Button>(R.id.openJavaActivity)

        openJavaActivity.setOnClickListener {
            x ++
//            JavaUtils.open(this@MainActivity, 1234, true){
//                java, kotlin ->
//                toast("Result From JavaActivity: java=$java, kotlin=$kotlin" )
//                textView.text = "Result From JavaActivity: java=$java, kotlin=$kotlin, x=$x"
//            }
            textView.startJavaActivity(1234, true){
                java, kotlin ->
                toast("Result From JavaActivity: java=$java, kotlin=$kotlin" )
                textView.text = "Result From JavaActivity: java=$java, kotlin=$kotlin, x=$x"
                button.setText(java)
            }
        }

        openKotlinActivity.setOnClickListener {
            Log.d("Main", "leave: "+this@MainActivity.toString())
            openKotlinActivity(1234){
                java, kotlin ->
                toast("Result From KotlinActivity: java=$java, kotlin=$kotlin")
                textView.text = "Result From JavaActivity: java=$java, kotlin=$kotlin"
                Log.d("Main", "back: " + this@MainActivity.toString())
            }
        }

        openGenerateBothActivity.setOnClickListener {
            openGenerateBothActivity(30, "bennyhuo", num = 1234)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("test", "resultCode: $resultCode, data: $data")
    }
}
