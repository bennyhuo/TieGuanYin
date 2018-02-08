package com.bennyhuo.tieguanyin.runtime.core;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bennyhuo.tieguanyin.runtime.result.ResultFragment;
import com.bennyhuo.tieguanyin.runtime.result.ListenerEnvironment;

import java.util.ArrayList;

/**
 * Created by benny on 1/30/18.
 */

public class ActivityBuilder {

    public final static ActivityBuilder INSTANCE = new ActivityBuilder();
    private Application application;

    private ArrayList<OnActivityCreateListener> onActivityCreateListeners = new ArrayList<>();

    private ArrayList<ListenerEnvironment> listenerEnvironments = new ArrayList<>();

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            ArrayList<OnActivityCreateListener> onActivityCreateListeners = (ArrayList<OnActivityCreateListener>) ActivityBuilder.this.onActivityCreateListeners.clone();
            for (OnActivityCreateListener onActivityCreateListener : onActivityCreateListeners) {
                onActivityCreateListener.onActivityCreated(activity, savedInstanceState);
            }
            FragmentBuilder.INSTANCE.onActivityCreated(activity);
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
            FragmentBuilder.INSTANCE.onActivityDestroyed(activity);
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

    public void addOnActivityResultListener(OnActivityResultListener onActivityResultListener){
        listenerEnvironments.add(new ListenerEnvironment(onActivityResultListener));
    }

    public void removeOnActivityResultListener(OnActivityResultListener onActivityResultListener){
        for (ListenerEnvironment listenerEnv : this.listenerEnvironments) {
            if(listenerEnv.onActivityResultListener == onActivityResultListener){
                this.listenerEnvironments.remove(listenerEnv);
                break;
            }
        }
    }

    public OnActivityResultListener findProbableOnResultListener(ResultFragment resultFragment, int hashCode){
        for (ListenerEnvironment listenerEnvironment : listenerEnvironments) {
            if(listenerEnvironment.onActivityResultListener.hashCode() == hashCode) {
                if(resultFragment.getActivity() == null){
                    Log.e("listenerEnv", "activity == null");
                } else {
                    listenerEnvironment.update(resultFragment);
                }
                return listenerEnvironment.onActivityResultListener;
            }
        }
        return null;
    }

    public void startActivityForResult(Activity activity, Intent intent, OnActivityResultListener onActivityResultListener) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(ResultFragment.TAG);
        ResultFragment resultFragment;
        if (fragment != null && fragment instanceof ResultFragment) {
            resultFragment = (ResultFragment) fragment;
        } else {
            resultFragment = new ResultFragment();
            fragmentManager.beginTransaction()
                    .add(resultFragment, ResultFragment.TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        resultFragment.setOnActivityResultListener(onActivityResultListener);
        resultFragment.startActivityForResult(intent, 1);
        addOnActivityResultListener(onActivityResultListener);
    }
}
