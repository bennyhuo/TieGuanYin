package com.bennyhuo.activitybuilder.runtime.core;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bennyhuo.activitybuilder.runtime.result.ResultFragment;
import com.bennyhuo.activitybuilder.runtime.result.fields.ActivityField;
import com.bennyhuo.activitybuilder.runtime.result.fields.FragmentField;
import com.bennyhuo.activitybuilder.runtime.result.fields.ListenerEnv;
import com.bennyhuo.activitybuilder.runtime.result.fields.ViewField;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by benny on 1/30/18.
 */

public class ActivityBuilder {

    public final static ActivityBuilder INSTANCE = new ActivityBuilder();
    private Application application;

    private ArrayList<OnActivityCreateListener> onActivityCreateListeners = new ArrayList<>();

    private ArrayList<ListenerEnv> listenerEnvs  = new ArrayList<>();

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
        ListenerEnv env = new ListenerEnv(onActivityResultListener);
        try {
            Field realListenerField = onActivityResultListener.getClass().getDeclaredFields()[0];
            realListenerField.setAccessible(true);

            Object obj = realListenerField.get(onActivityResultListener);
            Class cls = obj.getClass();

            while (!cls.isAnonymousClass()){
                Log.e("listenerEnv", "find probable class: " + cls.toString());
                realListenerField = cls.getDeclaredFields()[0];
                realListenerField.setAccessible(true);
                obj = realListenerField.get(obj);
                cls = obj.getClass();
            }

            while (cls.getEnclosingClass() != null){
                Class enclosingClass = cls.getEnclosingClass();
                Object enclosingObj = null;
                boolean hasRefOfEnclosingClass = false;
                Log.d("listenerEnv", cls.toString());
                for (Field field : cls.getDeclaredFields()) {
                    if(View.class.isAssignableFrom(field.getType())){
                        int id = ((View)field.get(obj)).getId();
                        env.viewFields.add(new ViewField(obj, field, id));
                    } else if(Fragment.class.isAssignableFrom(field.getType())){
                        int id = ((Fragment)field.get(obj)).getId();
                        env.fragmentFields.add(new FragmentField(obj, field, id));
                    } else if(android.support.v4.app.Fragment.class.isAssignableFrom(field.getType())){
                        int id = ((android.support.v4.app.Fragment)field.get(obj)).getId();
                        env.fragmentFields.add(new FragmentField(obj, field, id));
                    } else if(Activity.class.isAssignableFrom(field.getType())){
                        env.activityField = new ActivityField(obj, field);
                    }

                    if(field.getType() == enclosingClass){
                        hasRefOfEnclosingClass = true;
                        enclosingObj = field.get(obj);
                    }
                }
                if(hasRefOfEnclosingClass){
                    cls = enclosingClass;
                    obj = enclosingObj;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        listenerEnvs.add(env);
    }

    public void removeOnActivityResultListener(OnActivityResultListener onActivityResultListener){
        for (ListenerEnv listenerEnv : this.listenerEnvs) {
            if(listenerEnv.onActivityResultListener == onActivityResultListener){
                this.listenerEnvs.remove(listenerEnv);
                break;
            }
        }
    }

    public OnActivityResultListener findProbableOnResultListener(ResultFragment resultFragment, int hashCode){
        for (ListenerEnv listenerEnv : listenerEnvs) {
            if(listenerEnv.onActivityResultListener.hashCode() == hashCode) {
                if(resultFragment.getActivity() == null){
                    Log.e("listenerEnv", "activity == null");
                } else {
                    if(listenerEnv.activityField != null) {
                        listenerEnv.activityField.apply(resultFragment.getActivity());
                    }
                    for (ViewField viewField: listenerEnv.viewFields) {
                        viewField.apply(resultFragment.getActivity().findViewById(viewField.id));
                    }
                    for (FragmentField fragmentField : listenerEnv.fragmentFields) {
                        fragmentField.apply(resultFragment.getActivity().getFragmentManager().findFragmentById(fragmentField.id));
                    }
                }
                return listenerEnv.onActivityResultListener;
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
