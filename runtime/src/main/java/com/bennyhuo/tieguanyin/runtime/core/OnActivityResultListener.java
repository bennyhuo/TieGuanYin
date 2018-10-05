package com.bennyhuo.tieguanyin.runtime.core;

import android.os.Bundle;

/**
 * Created by benny on 1/31/18.
 */

public abstract class OnActivityResultListener {

    public final Object realListener;

    public OnActivityResultListener(Object realListener) {
        this.realListener = realListener;
    }

    public abstract void onResult(Bundle bundle);
}
