package com.bennyhuo.activitybuilder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bennyhuo.activitybuilder.annotations.ActivityBuilder
import com.bennyhuo.activitybuilder.fragments.showKotlinFragment

/**
 * Created by benny on 1/29/18.
 */
@ActivityBuilder
class FragmentContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)
        title = this.javaClass.simpleName

        showKotlinFragment(R.id.fragmentContainer, "Kotlin!!")
    }
}
