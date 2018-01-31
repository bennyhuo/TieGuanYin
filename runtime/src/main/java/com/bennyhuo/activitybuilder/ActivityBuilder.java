package com.bennyhuo.activitybuilder;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by benny on 1/30/18.
 */

public class ActivityBuilder {

    public final static ActivityBuilder INSTANCE = new ActivityBuilder();
    private Application application;

    private ArrayList<OnActivityCreateListener> onActivityCreateListeners = new ArrayList<>();

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            ArrayList<OnActivityCreateListener> onActivityCreateListeners = (ArrayList<OnActivityCreateListener>) ActivityBuilder.this.onActivityCreateListeners.clone();
            for (OnActivityCreateListener onActivityCreateListener : onActivityCreateListeners) {
                onActivityCreateListener.onActivityCreated(activity, savedInstanceState);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    private ActivityBuilder(){

    }

    public void init(Context context){
        if(this.application != null) return;
        this.application = (Application) context.getApplicationContext();
        this.application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public void addOnActivityCreateListener(OnActivityCreateListener onActivityCreateListener){
        this.onActivityCreateListeners.add(onActivityCreateListener);
    }

    public void removeOnActivityCreateListener(OnActivityCreateListener onActivityCreateListener){
        this.onActivityCreateListeners.remove(onActivityCreateListener);
    }

    public void setListenerForResult(Activity activity, OnActivityResultListener onActivityResultListener) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(ResultFragment.TAG);
        ResultFragment resultFragment;
        if (fragment != null && fragment instanceof ResultFragment) {
            resultFragment = (ResultFragment) fragment;
        } else {
            resultFragment = new ResultFragment();
            fragmentManager.beginTransaction()
                    .remove(fragment)
                    .add(resultFragment, ResultFragment.TAG)
                    .commitAllowingStateLoss();
        }
        resultFragment.setOnActivityResultListener(onActivityResultListener);
    }
}
