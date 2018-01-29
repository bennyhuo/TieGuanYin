package com.bennyhuo.activitybuilder;

import android.support.v7.app.AppCompatActivity;

import com.bennyhuo.annotations.GenerateBuilder;
import com.bennyhuo.annotations.Required;

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder
public class HelloInJavaActivity extends AppCompatActivity {

    @Required("num")
    int num;

}
