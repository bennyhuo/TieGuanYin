package com.bennyhuo.tieguanyin.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.util.Log
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.sample.inner.startInnerClass
import com.bennyhuo.tieguanyin.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

@Builder
class MainActivity : AppCompatActivity() {

    @Required
    var num: Int = 0

    @Required
    var string = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.sharedElementExitTransition =
            TransitionSet().addTransition(ChangeBounds()).addTransition(ChangeTransform())
        setContentView(R.layout.activity_main)
        title = this.javaClass.simpleName

        openJavaActivity.setOnClickListener {
            JavaActivityBuilder.builder(1)
                .isJava(true)
                .start(this) { hello, javaMethod, kotlin ->

                }
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
        }

        openMainActivity.setOnClickListener {
            startMainActivity(1024, Math.random().toString())
        }

        openParcelableActivity.setOnClickListener {
            startParcelableActivity(UserInfo("benny", 1, Company("yfd", "Beijing"))) {
                toast(it.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("test", "resultCode: $resultCode, data: $data")
        textView.text = "onActivityResult -- "
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processNewIntent(intent)
        toast("onNewIntent: num=$num, string=$string")
    }
}
