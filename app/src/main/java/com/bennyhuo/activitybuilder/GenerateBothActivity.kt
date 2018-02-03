package com.bennyhuo.activitybuilder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.activitybuilder.runtime.annotations.GenerateBuilder
import com.bennyhuo.activitybuilder.runtime.annotations.GenerateMode.Both
import com.bennyhuo.activitybuilder.runtime.annotations.Optional
import com.bennyhuo.activitybuilder.runtime.annotations.Required
import com.bennyhuo.activitybuilder.runtime.factory.ObjectCreator
import com.bennyhuo.activitybuilder.utils.toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.Serializable

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder(mode = Both)
class GenerateBothActivity : AppCompatActivity() {

    @Required()
    lateinit var name: String

    @Required()
    var age: Int = 0

    @Optional(intValue = 123)
    var num: Int = 0

    @Optional(stringValue = "I'm title!!")
    lateinit var title: String

    @Optional()
    lateinit var subTitle: String

    @Optional(creator = DetailsCreator::class)
    lateinit var details: Serializable

    class DetailsCreator: ObjectCreator<Serializable>{
        override fun create(cls: Class<Serializable>):Serializable {
            return "details..."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(this.javaClass.simpleName)
        openJavaActivity.setOnClickListener {
            JavaActivityBuilder.open(this@GenerateBothActivity, 1234, true){
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

        openGenerateBothActivity.text = "Finish Me!"
        openGenerateBothActivity.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
    }

    companion object {

    }
}