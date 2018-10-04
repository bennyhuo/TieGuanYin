package com.bennyhuo.tieguanyin.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import android.view.ViewGroup
import com.bennyhuo.tieguanyin.sample.inner.startInnerClass
import com.bennyhuo.tieguanyin.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.sharedElementExitTransition = TransitionSet().addTransition(ChangeBounds()).addTransition(ChangeTransform())
        setContentView(R.layout.activity_main)
        setTitle(this.javaClass.simpleName)

        openJavaActivity.setOnClickListener {
            //startDetailsActivity()
            Log.d("Main", it.rootView.toString())
            val decorView = this.window.decorView as ViewGroup
//            Log.d("Main", decorView.toString())
//            for (i in 0 until decorView.childCount){
//                Log.d("Main", decorView.getChildAt(i).context.toString())
//            }
        }

        openKotlinActivity.setOnClickListener {
            Log.d("Main", "leave: " + this@MainActivity.toString())
            startKotlinActivity(1234) { java, kotlin ->
                toast("Result From JavaActivity: java=${java.contentToString()}, kotlin=${kotlin.contentToString()}")
            }
        }

        openGenerateBothActivity.setOnClickListener {
            startGenerateBothActivity(30, "bennyhuo", num = 1234)
        }

        openFragmentContainerActivity.setOnClickListener {
            startFragmentContainerActivity()
        }

        openInnerActivity.setOnClickListener {
            startInnerClass(2, true)

            startUserActivity(30, "bennyhuo", "Kotliner", "Kotlin Developer")
        }

        startEditUserActivity(36, "Kotliner", "bennyhuo", "Kotlin Dev"){
            age, company, name, title ->

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("test", "resultCode: $resultCode, data: $data")
        textView.text = "onActivityResult -- "
    }
}
