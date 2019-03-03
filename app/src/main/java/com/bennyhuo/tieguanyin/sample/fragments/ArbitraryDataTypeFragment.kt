package com.bennyhuo.tieguanyin.sample.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.annotations.Optional
import com.bennyhuo.tieguanyin.annotations.Required
import com.bennyhuo.tieguanyin.sample.R
import com.bennyhuo.tieguanyin.sample.data.Person
import com.bennyhuo.tieguanyin.sample.transitions.replaceDetailsFragment
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by benny on 2/6/18.
 */
@Builder
class ArbitraryDataTypeFragment : Fragment() {

    @Required
    lateinit var text: String

    @Optional
    var person: Person? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setTransitionName(textView, "fragment")
        textView.text = "$text\n$person"
        textView.setOnClickListener {
            replaceDetailsFragment("Kotlin Fragment")
        }
    }
}
