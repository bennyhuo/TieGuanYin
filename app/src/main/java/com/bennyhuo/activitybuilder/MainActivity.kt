package com.bennyhuo.activitybuilder

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bennyhuo.activitybuilder.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(this.javaClass.simpleName)
        openJavaActivity.setOnClickListener {
            JavaActivityBuilder.open(this@MainActivity, 1234, true){
                java, kotlin ->
                toast("Result From JavaActivity: java=$java, kotlin=$kotlin")
            }
        }

        openKotlinActivity.setOnClickListener {
            openKotlinActivity(1234){
                java, kotlin ->
                toast("Result From KotlinActivity: java=$java, kotlin=$kotlin")
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
