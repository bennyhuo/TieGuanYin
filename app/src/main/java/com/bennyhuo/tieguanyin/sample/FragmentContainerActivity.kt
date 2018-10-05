package com.bennyhuo.tieguanyin.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.tieguanyin.annotations.Builder
import com.bennyhuo.tieguanyin.sample.fragments.replaceKotlinFragment

/**
 * Created by benny on 1/29/18.
 */
@Builder
class FragmentContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)
        title = this.javaClass.simpleName

        if(supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null)
            replaceKotlinFragment(R.id.fragmentContainer, "Kotlin Fragment")
    }
}
