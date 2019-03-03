package com.bennyhuo.tieguanyin.sample

import android.app.Application
import com.bennyhuo.tieguanyin.runtime.Tieguanyin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Tieguanyin.DEBUG = true
        Tieguanyin.init(this)
    }
}