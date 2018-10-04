package com.bennyhuo.tieguanyin.sample.transitions

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.SharedElement
import com.bennyhuo.tieguanyin.annotations.SharedElementByNames
import com.bennyhuo.tieguanyin.annotations.SharedElementWithName
import com.bennyhuo.tieguanyin.sample.R
import kotlinx.android.synthetic.main.activity_transitions.*

@Builder(
        sharedElements = [SharedElement(sourceId = R.id.openJavaActivity, targetName = "hello")],
        sharedElementsWithName = [(SharedElementWithName("button2"))],
        sharedElementsByNames = [(SharedElementByNames(source = "button1", target = "button3"))]
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
