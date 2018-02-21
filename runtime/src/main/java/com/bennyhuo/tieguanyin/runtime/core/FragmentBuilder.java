package com.bennyhuo.tieguanyin.runtime.core;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentUtils;
import android.support.v4.util.Pair;
import android.transition.AutoTransition;

import com.bennyhuo.tieguanyin.runtime.utils.Logger;

import java.util.ArrayList;

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
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(callbacks, true);
        }
    }

    void onActivityDestroyed(Activity activity){
        if(activity instanceof FragmentActivity){
            ((FragmentActivity) activity).getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(callbacks);
        }
    }

    public static void showFragment(FragmentActivity activity, int containerId, Bundle args, Class<? extends Fragment> fragmentCls, ArrayList<Pair<String, String>> sharedElements) {
        try {
            Fragment fragment = fragmentCls.newInstance();
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
                fragment.setSharedElementEnterTransition(new AutoTransition());
            }
            fragment.setArguments(args);
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction().replace(containerId, fragment);
            if(sharedElements != null){
                for (Pair<String, String> sharedElement : sharedElements) {
                    if(sharedElement.first != null) {
                        FragmentUtils.addSharedElement(transaction, sharedElement.first, sharedElement.second);
                    }
                }
            }
            transaction.commit();
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
