package com.bennyhuo.tieguanyin.runtime.core;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.transition.AutoTransition;

import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.SupportFragmentUtils;

import com.bennyhuo.tieguanyin.runtime.utils.Logger;

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
            savedInstanceState = savedInstanceState == null ? fragment.getArguments() : savedInstanceState;
            if(savedInstanceState == null) return;
            BuilderClassFinder.findBuilderClass(fragment).getDeclaredMethod("inject", Fragment.class, Bundle.class).invoke(null, fragment, savedInstanceState);
            Logger.debug("inject success: fragment=" + fragment + ", state=" + savedInstanceState);
        } catch (Exception e) {
            Logger.warn("inject failed: fragment=" + fragment + ", state=" + savedInstanceState + ", e=" + e);
        }
    }

    private void performSaveState(Fragment fragment, Bundle outState){
        try {
            BuilderClassFinder.findBuilderClass(fragment).getDeclaredMethod("saveState", Fragment.class, Bundle.class).invoke(null, fragment, outState);
            Logger.debug("save success: fragment=" + fragment + ", state=" + outState);
        } catch (Exception e) {
            Logger.warn("save failed: fragment=" + fragment + ", state=" + outState + ", e=" + e);
        }
    }

    void onActivityCreated(Activity activity){
        if(activity instanceof FragmentActivity){
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(callbacks, true);
        }
    }

    void onActivityDestroyed(Activity activity){
        if(activity instanceof FragmentActivity){
            ((FragmentActivity) activity).getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
        }
    }

    public static <T  extends Fragment> T showFragment(FragmentActivity activity, boolean isReplace, int containerId, String tag, Bundle args, Class<T> fragmentCls, ArrayList<Pair<String, String>> sharedElements) {
        try {
            Fragment fragment = fragmentCls.newInstance();
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
                fragment.setSharedElementEnterTransition(new AutoTransition());
            }
            fragment.setArguments(args);
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            if(isReplace){
                transaction.replace(containerId, fragment, tag);
            } else {
                transaction.add(containerId, fragment, tag);
            }
            if(sharedElements != null){
                for (Pair<String, String> sharedElement : sharedElements) {
                    if(sharedElement.first != null) {
                        SupportFragmentUtils.addSharedElement(transaction, sharedElement.first, sharedElement.second);
                    }
                }
            }
            transaction.commit();
            return (T) fragment;
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }
}
