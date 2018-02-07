package com.bennyhuo.activitybuilder.runtime.core;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

/**
 * Created by benny on 2/7/18.
 */

public class FragmentBuilder {
    public static FragmentBuilder INSTANCE = new FragmentBuilder();

    private FragmentManager.FragmentLifecycleCallbacks callbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
            super.onFragmentCreated(fm, f, savedInstanceState);
            for (OnFragmentCreateListener onFragmentCreatedListener : onFragmentCreatedListeners) {
                onFragmentCreatedListener.onFragmentCreated(f, savedInstanceState);
            }
        }
    };

    private ArrayList<OnFragmentCreateListener> onFragmentCreatedListeners = new ArrayList<>();

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

    public void addOnFragmentCreateListener(OnFragmentCreateListener onFragmentCreatedListener){
        onFragmentCreatedListeners.add(onFragmentCreatedListener);
    }

    public void removeOnFragmentCreateListener(OnFragmentCreateListener onFragmentCreatedListener){
        onFragmentCreatedListeners.remove(onFragmentCreatedListener);
    }
}
