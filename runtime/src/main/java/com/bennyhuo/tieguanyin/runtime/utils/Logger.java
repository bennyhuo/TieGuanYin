package com.bennyhuo.tieguanyin.runtime.utils;

import android.util.Log;

/**
 * Created by benny on 2/9/18.
 */

public class Logger {
    public static final String TAG = "TieGuanYin";

    public static boolean isDebug = false;

    public static void debug(Object log){
        if(isDebug) Log.d(TAG, String.valueOf(log));
    }

    public static void error(Object log){
        if(isDebug) Log.e(TAG, String.valueOf(log));
    }

    public static void warn(Object log){
        if(isDebug) Log.w(TAG, String.valueOf(log));
    }
}
