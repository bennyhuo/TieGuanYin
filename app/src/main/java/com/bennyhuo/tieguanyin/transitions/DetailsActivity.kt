package com.bennyhuo.tieguanyin.transitions

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bennyhuo.tieguanyin.R
import com.bennyhuo.tieguanyin.annotations.ActivityBuilder
import com.bennyhuo.tieguanyin.annotations.SharedElement
import kotlinx.android.synthetic.main.activity_main.*

@ActivityBuilder(
        sharedElements = [SharedElement(viewId = R.id.openJavaActivity, transitionName = "hello")]
)
class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transitions)
        setTitle(this.javaClass.simpleName)

        openJavaActivity.transitionName = "hello"
        openJavaActivity.setOnClickListener {

        }

        openKotlinActivity.setOnClickListener {

        }

        openGenerateBothActivity.setOnClickListener {

        }

        openFragmentContainerActivity.setOnClickListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("test", "resultCode: $resultCode, data: $data")
        textView.text = "onActivityResult -- "
    }
}
