package com.bennyhuo.tieguanyin.runtime.result;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.SupportFragmentUtils;

import com.bennyhuo.tieguanyin.runtime.core.OnActivityResultListener;
import com.bennyhuo.tieguanyin.runtime.result.fields.ActivityField;
import com.bennyhuo.tieguanyin.runtime.result.fields.FragmentField;
import com.bennyhuo.tieguanyin.runtime.result.fields.ViewField;
import com.bennyhuo.tieguanyin.runtime.utils.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by benny on 2/6/18.
 */

public class ListenerEnvironment {
    public final OnActivityResultListener onActivityResultListener;
    private ActivityField activityField;
    private final ArrayList<FragmentField> fragmentFields = new ArrayList<>();
    private final ArrayList<ViewField> viewFields = new ArrayList<>();

    public ListenerEnvironment(OnActivityResultListener onActivityResultListener) {
        this.onActivityResultListener = onActivityResultListener;

        try {
//            Field realListenerField = onActivityResultListener.getClass().getDeclaredFields()[0];
//            realListenerField.setAccessible(true);

            Object obj = onActivityResultListener.realListener;
            Class cls = obj.getClass();
            Field outerRefField;

            while (!cls.isAnonymousClass()){
                Log.e("listenerEnv", "find probable class: " + cls.toString());
                // 注意这个其实是 $this 引用，指向外部类实例
                outerRefField = cls.getDeclaredFields()[0];
                outerRefField.setAccessible(true);
                Object outerObject = outerRefField.get(obj);
                // 内联的 lambda
                if (outerObject == obj) break;
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
                        String who = SupportFragmentUtils.getWhoFromFragment((Fragment)field.get(obj));
                        fragmentFields.add(new FragmentField(obj, field, who));
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
            Logger.warn(e);
        }
    }

    public void update(ResultFragment resultFragment){
        if(activityField != null) {
            activityField.update(resultFragment.getActivity());
        }
        for (ViewField viewField: viewFields) {
            viewField.update(resultFragment.getActivity().findViewById(viewField.id));
        }
        if(resultFragment.getActivity() instanceof FragmentActivity){
            FragmentActivity fragmentActivity = (FragmentActivity) resultFragment.getActivity();
            for (FragmentField fragmentField : fragmentFields) {
                Fragment fragment = SupportFragmentUtils.findFragmentByWho(fragmentActivity.getSupportFragmentManager(), fragmentField.who);
                Logger.debug("Update, Who: " + fragmentField.who + ", fragment=" + fragment);
                fragmentField.update(fragment);
            }
        }
    }
}
