package com.bennyhuo.activitybuilder

import android.app.Application

/**
 * Created by benny on 1/30/18.
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        //ActivityBuilder.INSTANCE.init(this)
    }
}