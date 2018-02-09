package com.bennyhuo.tieguanyin.runtime.core;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.bennyhuo.tieguanyin.runtime.utils.Logger;

/**
 * Created by benny on 2/7/18.
 */

public class FragmentBuilder {
    public static final String BUILDER_NAME_POSIX = "Builder";

    public static FragmentBuilder INSTANCE = new FragmentBuilder();

    private FragmentManager.FragmentLifecycleCallbacks callbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
            super.onFragmentCreated(fm, f, savedInstanceState);
            performInject(f, savedInstanceState);
        }

        @Override
        public void onFragmentSaveInstanceState(FragmentManager fm, Fragment f, Bundle outState) {
            super.onFragmentSaveInstanceState(fm, f, outState);
            performSaveState(f, outState);
        }
    };

    private void performInject(Fragment fragment, Bundle savedInstanceState){
        try {
            Class.forName(fragment.getClass().getName() + BUILDER_NAME_POSIX).getDeclaredMethod("inject", Fragment.class, Bundle.class).invoke(null, fragment, savedInstanceState);
        } catch (Exception e) {
            Logger.warn(e);
        }
    }

    private void performSaveState(Fragment fragment, Bundle outState){
        try {
            Class.forName(fragment.getClass().getName() + BUILDER_NAME_POSIX).getDeclaredMethod("saveState", Fragment.class, Bundle.class).invoke(null, fragment, outState);
        } catch (Exception e) {
            Logger.warn(e);
        }
    }

    void onActivityCreated(Activity activity){
        if(activity instanceof FragmentActivity){
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(callbacks, false);
        }
    }

    void onActivityDestroyed(Activity activity){
        if(activity instanceof FragmentActivity){
            ((FragmentActivity) activity).getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
        }
    }
}
