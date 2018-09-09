package com.bennyhuo.tieguanyin.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.tieguanyin.annotations.*
import com.bennyhuo.tieguanyin.annotations.GenerateMode.Both
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by benny on 1/29/18.
 */
@Builder(mode = Both,
        pendingTransition = PendingTransition(enterAnim = 0, exitAnim = 0),
        pendingTransitionOnFinish = PendingTransition(enterAnim = 0, exitAnim = 0),
        resultTypes = [(ResultEntity(name = "java", type = Array<String>::class)), (ResultEntity(name = "kotlin", type = IntArray::class))],
        flags = [Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT, Intent.FLAG_ACTIVITY_CLEAR_TOP],
        categories = [Intent.CATEGORY_APP_BROWSER, Intent.CATEGORY_APP_CALENDAR]
)
class KotlinActivity : AppCompatActivity() {

    @Required
    var num: Int = 0

    @Optional
    var java: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(this.javaClass.simpleName)
        openJavaActivity.setOnClickListener {
//            JavaActivityBuilder.open(this@KotlinActivity, 1234, true){
//                java, kotlin ->
//                toast("Result From JavaActivity: java=$java, kotlin=$kotlin")
//            }
        }

        openKotlinActivity.text = "Finish With java='I'm Kotlin!' & kotlin=12"
        openKotlinActivity.setOnClickListener {
            finishWithResult(arrayOf("I'm Kotlin!"), intArrayOf(12))
        }

        openGenerateBothActivity.setOnClickListener {
//            openGenerateBothActivity(30, "bennyhuo", num = 1234)
            num++
        }

        textView.text = "num=$num, java=$java"
    }
}
