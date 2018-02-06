package com.bennyhuo.activitybuilder.runtime.result.fields;

import com.bennyhuo.activitybuilder.runtime.core.OnActivityResultListener;

import java.util.ArrayList;

/**
 * Created by benny on 2/6/18.
 */

public class ListenerEnv {
    public final OnActivityResultListener onActivityResultListener;
    public ActivityField activityField;
    public ArrayList<FragmentField> fragmentFields = new ArrayList<>();
    public ArrayList<ViewField> viewFields = new ArrayList<>();

    public ListenerEnv(OnActivityResultListener onActivityResultListener) {
        this.onActivityResultListener = onActivityResultListener;
    }
}
