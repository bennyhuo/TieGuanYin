package com.bennyhuo.activitybuilder.runtime.core;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

/**
 * Created by benny on 1/31/18.
 */

public class ResultFragment extends Fragment {
    public static final String TAG = "com.bennyhuo.activitybuilder.ResultFragment";

    private OnActivityResultListener onActivityResultListener;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_CANCELED && data != null){
            onActivityResultListener.onResult(data.getExtras());
        }
        onActivityResultListener = null;
    }

    public void setOnActivityResultListener(OnActivityResultListener onActivityResultListener) {
        this.onActivityResultListener = onActivityResultListener;
    }
}
