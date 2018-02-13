package com.bennyhuo.tieguanyin.sample.transitions

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import com.bennyhuo.tieguanyin.annotations.ActivityBuilder
import com.bennyhuo.tieguanyin.annotations.SharedElement
import com.bennyhuo.tieguanyin.sample.R
import kotlinx.android.synthetic.main.activity_transitions.*

@ActivityBuilder(
        sharedElements = [SharedElement(viewId = R.id.openJavaActivity, transitionName = "hello")]
)
class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.sharedElementEnterTransition = TransitionSet().addTransition(ChangeBounds()).addTransition(ChangeTransform())
        setContentView(R.layout.activity_transitions)
        setTitle(this.javaClass.simpleName)

        openJavaActivity.transitionName = "hello"
    }
}
