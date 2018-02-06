package com.bennyhuo.activitybuilder.runtime.result;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bennyhuo.activitybuilder.runtime.core.ActivityBuilder;
import com.bennyhuo.activitybuilder.runtime.core.OnActivityResultListener;

/**
 * Created by benny on 1/31/18.
 */

public class ResultFragment extends Fragment {
    public static final String TAG = "com.bennyhuo.activitybuilder.ResultFragment";
    public static final String SAVE_STATE_KEY = "com.bennyhuo.activitybuilder.ResultFragment.SaveState";

    private OnActivityResultListener onActivityResultListener;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(onActivityResultListener != null && resultCode != Activity.RESULT_CANCELED && data != null){
            onActivityResultListener.onResult(data.getExtras());
            ActivityBuilder.INSTANCE.removeOnActivityResultListener(onActivityResultListener);
            onActivityResultListener = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(onActivityResultListener != null) {
            outState.putParcelable(SAVE_STATE_KEY, new SavedListenerState(onActivityResultListener.hashCode()));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(onActivityResultListener == null && savedInstanceState != null){
            Parcelable savedState = savedInstanceState.getParcelable(SAVE_STATE_KEY);
            if(savedState != null && savedState instanceof SavedListenerState){
                onActivityResultListener = ActivityBuilder.INSTANCE.findProbableOnResultListener(this, ((SavedListenerState) savedState).savedListenerHashCode);
            }
        }
    }

    public void setOnActivityResultListener(OnActivityResultListener onActivityResultListener) {
        this.onActivityResultListener = onActivityResultListener;
    }

    public static class SavedListenerState implements Parcelable {
        int savedListenerHashCode = -1;

        public SavedListenerState(int savedListenerHashCode) {
            this.savedListenerHashCode = savedListenerHashCode;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.savedListenerHashCode);
        }

        protected SavedListenerState(Parcel in) {
            this.savedListenerHashCode = in.readInt();
        }

        public static final Creator<SavedListenerState> CREATOR = new Creator<SavedListenerState>() {
            @Override
            public SavedListenerState createFromParcel(Parcel source) {
                return new SavedListenerState(source);
            }

            @Override
            public SavedListenerState[] newArray(int size) {
                return new SavedListenerState[size];
            }
        };
    }

}
