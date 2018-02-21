package com.bennyhuo.tieguanyin.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.tieguanyin.annotations.ActivityBuilder
import com.bennyhuo.tieguanyin.annotations.GenerateMode.KotlinOnly
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.annotations.ResultEntity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by benny on 1/29/18.
 */
@ActivityBuilder(mode = KotlinOnly,
        resultTypes = [(ResultEntity(name = "java", type = String::class)), (ResultEntity(name = "kotlin", type = Int::class))],
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
            finishWithResult("I'm Kotlin!", 12)
        }

        openGenerateBothActivity.setOnClickListener {
//            openGenerateBothActivity(30, "bennyhuo", num = 1234)
            num++
        }

        textView.text = "num=$num, java=$java"
    }
}
