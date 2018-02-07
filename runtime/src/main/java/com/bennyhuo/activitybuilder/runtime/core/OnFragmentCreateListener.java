package com.bennyhuo.activitybuilder.runtime.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public interface OnFragmentCreateListener {
    void onFragmentCreated(Fragment fragment, Bundle savedInstanceState);
}