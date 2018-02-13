package com.bennyhuo.tieguanyin.sample.transitions

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bennyhuo.tieguanyin.annotations.FragmentBuilder
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.annotations.SharedElement
import com.bennyhuo.tieguanyin.sample.R
import kotlinx.android.synthetic.main.fragment_transitions.*

/**
 * Created by benny on 2/6/18.
 */
@FragmentBuilder(
        sharedElements = [SharedElement(viewId = R.id.textView, transitionName = "fragment")]
)
class DetailsFragment : Fragment() {

    @Required
    lateinit var text: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transitions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(textView, "fragment")
        textView.text = text
    }
}
