package com.bennyhuo.tieguanyin.runtime;

import android.content.Context;
import android.content.Intent;

/**
 * Created by benny at 2021/11/30 11:45 AM.
 */
public interface ActivityBuilderCallback {

    void beforeStartActivity(Context context, Intent intent);

}
