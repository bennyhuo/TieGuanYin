package com.bennyhuo.activitybuilder;

import android.app.Activity;

import com.bennyhuo.annotations.GenerateBuilder;
import com.bennyhuo.annotations.Required;

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder
public class HelloInJavaActivity extends Activity {

    @Required("num")
    int num;

}
