package com.bennyhuo.utils;

import android.os.Bundle;

/**
 * Created by benny on 1/30/18.
 */

public class BundleUtils {
    public static <T> T get(Bundle bundle, String key){
        return (T) bundle.get(key);
    }

    public static <T> T get(Bundle bundle, String key, T defaultValue){
        Object obj = bundle.get(key);
        if(obj == null){
            return defaultValue;
        }
        return (T) obj;
    }
}
