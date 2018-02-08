package com.bennyhuo.tieguanyin.runtime.result;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.view.View;

import com.bennyhuo.tieguanyin.runtime.core.OnActivityResultListener;
import com.bennyhuo.tieguanyin.runtime.result.fields.ActivityField;
import com.bennyhuo.tieguanyin.runtime.result.fields.FragmentField;
import com.bennyhuo.tieguanyin.runtime.result.fields.ViewField;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by benny on 2/6/18.
 */

public class ListenerEnvironment {
    public final OnActivityResultListener onActivityResultListener;
    private ActivityField activityField;
    private ArrayList<FragmentField> fragmentFields = new ArrayList<>();
    private ArrayList<ViewField> viewFields = new ArrayList<>();

    public ListenerEnvironment(OnActivityResultListener onActivityResultListener) {
        this.onActivityResultListener = onActivityResultListener;

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
                    field.setAccessible(true);
                    if(View.class.isAssignableFrom(field.getType())){
                        int id = ((View)field.get(obj)).getId();
                        viewFields.add(new ViewField(obj, field, id));
                    } else if(Fragment.class.isAssignableFrom(field.getType())){
                        int id = ((Fragment)field.get(obj)).getId();
                        fragmentFields.add(new FragmentField(obj, field, id));
                    } else if(android.support.v4.app.Fragment.class.isAssignableFrom(field.getType())){
                        int id = ((android.support.v4.app.Fragment)field.get(obj)).getId();
                        fragmentFields.add(new FragmentField(obj, field, id));
                    } else if(Activity.class.isAssignableFrom(field.getType())){
                        activityField = new ActivityField(obj, field);
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
    }

    public void update(ResultFragment resultFragment){
        if(activityField != null) {
            activityField.update(resultFragment.getActivity());
        }
        for (ViewField viewField: viewFields) {
            viewField.update(resultFragment.getActivity().findViewById(viewField.id));
        }
        for (FragmentField fragmentField : fragmentFields) {
            fragmentField.update(resultFragment.getActivity().getFragmentManager().findFragmentById(fragmentField.id));
        }
    }
}
