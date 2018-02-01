package com.bennyhuo.activitybuilder

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clickMe.setOnClickListener {
            //HelloActivityBuilder.openWithOptionalTitle(this, 18, "bennyhuo", "kotlin")
            HelloInKotlinActivityBuilder.open(this, 12121212, false){
                java, kotlin ->
                Log.d("test", "java: $java, kotlin: $kotlin")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("test", "resultCode: $resultCode, data: $data")
    }
}
