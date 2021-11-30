package com.bennyhuo.tieguanyin.sample

import android.app.Application
import android.util.Log
import com.bennyhuo.tieguanyin.runtime.Tieguanyin

/**
 * Created by benny at 2021/11/30 11:57 AM.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Tieguanyin.init(this) { context, intent ->
            Log.d("tieguanyin", intent.toString())
        }
    }
}