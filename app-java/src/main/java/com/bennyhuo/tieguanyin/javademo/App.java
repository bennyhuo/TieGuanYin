package com.bennyhuo.tieguanyin.javademo;

import android.app.Application;

import com.bennyhuo.tieguanyin.runtime.Tieguanyin;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Tieguanyin.init(this);
    }
}
