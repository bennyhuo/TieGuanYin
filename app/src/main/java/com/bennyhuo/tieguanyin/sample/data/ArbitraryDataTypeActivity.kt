package com.bennyhuo.tieguanyin.sample.data

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.GenerateMode.Both
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.sample.JavaActivityBuilder
import com.bennyhuo.tieguanyin.sample.R
import com.bennyhuo.tieguanyin.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

@Builder(mode = Both
        //resultTypes = [(ResultEntity(name = "java", type = Array<String>::class)), (ResultEntity(name = "kotlin", type = IntArray::class))],
)
class ArbitraryDataTypeActivity : AppCompatActivity() {

    @Required
    lateinit var person: Person

    @Optional
    lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = this.javaClass.simpleName
        openJavaActivity.setOnClickListener {
            JavaActivityBuilder.builder(1234)
                    .start(this@ArbitraryDataTypeActivity) { javaMethod, java, kotlin ->
                        toast("Result From JavaActivity: javaMethod=$javaMethod, java=$java, kotlin=$kotlin")
                    }
        }

        openKotlinActivity.text = "Finish With java='I'm Kotlin!' & kotlin=12"
        openKotlinActivity.setOnClickListener {
            //smartFinish(arrayOf("I'm Java!", "You're Kotlin!"), intArrayOf(12))
        }

        textView.text = "person=$person, book=$book"
    }
}
