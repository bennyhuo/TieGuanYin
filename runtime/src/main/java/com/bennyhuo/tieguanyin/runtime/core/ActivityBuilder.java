package com.bennyhuo.tieguanyin.runtime.core;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bennyhuo.tieguanyin.annotations.PendingTransition;
import com.bennyhuo.tieguanyin.runtime.ActivityBuilderCallback;
import com.bennyhuo.tieguanyin.runtime.result.ListenerEnvironment;
import com.bennyhuo.tieguanyin.runtime.result.ResultFragment;
import com.bennyhuo.tieguanyin.runtime.utils.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by benny on 1/30/18.
 */

public class ActivityBuilder {
    public final static ActivityBuilder INSTANCE = new ActivityBuilder();
    private Application application;
    private WeakReference<Activity> currentActivityRef;
    private ActivityBuilderCallback builderCallback;

    private ArrayList<ListenerEnvironment> listenerEnvironments = new ArrayList<>();

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            performInject(activity, savedInstanceState);
            FragmentBuilder.INSTANCE.onActivityCreated(activity);
        }
        //region unused
        @Override
        public void onActivityStarted(Activity activity) {
            currentActivityRef = new WeakReference<>(activity);
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
        //endregion
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            performSaveState(activity, outState);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            FragmentBuilder.INSTANCE.onActivityDestroyed(activity);
        }
    };

    private ActivityBuilder(){}

    public void init(Context context){
        if(this.application != null) return;
        this.application = (Application) context.getApplicationContext();
        this.application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public void setBuilderCallback(ActivityBuilderCallback builderCallback) {
        this.builderCallback = builderCallback;
    }

    private void performInject(Activity activity, Bundle savedInstanceState){
        try {
            if(savedInstanceState == null){
                Intent intent = activity.getIntent();
                if(intent == null){
                    return;
                }
                savedInstanceState = intent.getExtras();
            }
            BuilderClassFinder.findBuilderClass(activity).getDeclaredMethod("inject", Activity.class, Bundle.class).invoke(null, activity, savedInstanceState);
        } catch (Exception e) {
            Logger.warn(e);
        }
    }

    private void performSaveState(Activity activity, Bundle outState){
        try {
            BuilderClassFinder.findBuilderClass(activity).getDeclaredMethod("saveState", Activity.class, Bundle.class).invoke(null, activity, outState);
        } catch (Exception e) {
            Logger.warn(e);
        }
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

    public void startActivityForResult(Context context, Intent intent, Bundle options, int enterAnim, int exitAnim, OnActivityResultListener onActivityResultListener) {
        if (builderCallback != null) {
            builderCallback.beforeStartActivity(context, intent);
        }
        if(context instanceof Activity){
            Activity activity = (Activity)context;
            if(onActivityResultListener == null){
                ActivityCompat.startActivityForResult(activity, intent, 1, options);
            } else {
                FragmentManager fragmentManager = activity.getFragmentManager();
                Fragment fragment = fragmentManager.findFragmentByTag(ResultFragment.TAG);
                ResultFragment resultFragment;
                if (fragment instanceof ResultFragment) {
                    resultFragment = (ResultFragment) fragment;
                } else {
                    resultFragment = new ResultFragment();
                    fragmentManager.beginTransaction()
                            .add(resultFragment, ResultFragment.TAG)
                            .commitAllowingStateLoss();
                    fragmentManager.executePendingTransactions();
                }
                resultFragment.setOnActivityResultListener(onActivityResultListener);
                resultFragment.startActivityForResult(intent, 1, options);
                addOnActivityResultListener(onActivityResultListener);
            }
            if(enterAnim != PendingTransition.DEFAULT || exitAnim != PendingTransition.DEFAULT){
                activity.overridePendingTransition(enterAnim, exitAnim);
            }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void startActivity(Context context, Intent intent, Bundle options, int enterAnim, int exitAnim){
        if (builderCallback != null) {
            builderCallback.beforeStartActivity(context, intent);
        }
        if(context instanceof Activity) {
            Activity activity = (Activity) context;
            ActivityCompat.startActivity(activity, intent, options);
            if(enterAnim != PendingTransition.DEFAULT || exitAnim != PendingTransition.DEFAULT){
                activity.overridePendingTransition(enterAnim, exitAnim);
            }
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static Bundle makeSceneTransition(Context context, ArrayList<Pair<View, String>> sharedElements){
        if(context instanceof Activity) {
            Activity activity = (Activity) context;
            Iterator<Pair<View, String>> iterator = sharedElements.iterator();
            while (iterator.hasNext()){
                if(iterator.next().first == null) iterator.remove();
            }
            if(sharedElements.isEmpty()) {
                return null;
            }
            return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements.toArray(new Pair[0])).toBundle();
        } else {
            return null;
        }
    }

    public Activity findProperActivity(View view){
        if(currentActivityRef != null){
            Activity currentActivity = currentActivityRef.get();
            //大多数情况
            if(currentActivity != null && view.getRootView() == currentActivity.getWindow().getDecorView()){
                return currentActivity;
            }
        }
        Activity candidate = getActivity(view);
        //View 不是用 Activity 的实例创建
        if(candidate == null){
            View decorView = view.getRootView();
            if(decorView != null && decorView instanceof ViewGroup){
                ViewGroup viewGroup = (ViewGroup) decorView;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    candidate = getActivity(viewGroup.getChildAt(i));
                    if(candidate != null) break;
                }
            }
        }
        return candidate;
    }

    private static Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}
