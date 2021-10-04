package com.bennyhuo.tieguanyin.sample.transitions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.annotations.SharedElementWithName
import com.bennyhuo.tieguanyin.sample.R
import com.bennyhuo.tieguanyin.sample.fragments.JavaFragmentBuilder
import kotlinx.android.synthetic.main.fragment_transitions.*

/**
 * Created by benny on 2/6/18.
 */
@Builder(
        //sharedElements = [SharedElement(sourceId = R.id.textView, targetName = "fragment")]
        sharedElementsWithName = [SharedElementWithName("fragment")]
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
        textView.setOnClickListener {
            JavaFragmentBuilder.builder("Hello").num(1).num2(2.0).replace(requireActivity(), R.id.fragmentContainer)
        }
    }
}
