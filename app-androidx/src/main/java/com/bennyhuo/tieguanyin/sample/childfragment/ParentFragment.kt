package com.bennyhuo.tieguanyin.sample.childfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.sample.R
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by benny on 2/6/18.
 */
@Builder
class ParentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView.text = "show child fragment."
        textView.setOnClickListener {
            replaceChildFragment("ChildFragment.")
        }
    }
}