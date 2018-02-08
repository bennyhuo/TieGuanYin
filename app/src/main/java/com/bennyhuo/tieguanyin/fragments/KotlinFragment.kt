package com.bennyhuo.tieguanyin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bennyhuo.tieguanyin.R
import com.bennyhuo.tieguanyin.annotations.FragmentBuilder
import com.bennyhuo.tieguanyin.annotations.Required
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by benny on 2/6/18.
 */
@FragmentBuilder
class KotlinFragment : Fragment() {

    @Required
    lateinit var text: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView.text = text
        textView.setOnClickListener {
            showJavaFragment("Java!!")
        }
    }
}
