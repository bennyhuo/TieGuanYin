package com.bennyhuo.tieguanyin.runtime.utils;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;

import java.util.Map;

/**
 * Created by benny on 2/13/18.
 */

public class ViewUtils {
    public static void findNamedViews(View view, Map<String, View> namedViews) {
        if (view.getVisibility() == View.VISIBLE) {
            String transitionName = ViewCompat.getTransitionName(view);
            if (transitionName != null) {
                namedViews.put(transitionName, view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = viewGroup.getChildAt(i);
                    findNamedViews(child, namedViews);
                }
            }
        }
    }
}
