package com.bennyhuo.activitybuilder;

import android.support.v7.app.AppCompatActivity;

import com.bennyhuo.annotations.GenerateBuilder;
import com.bennyhuo.annotations.Optional;
import com.bennyhuo.annotations.Required;
import com.bennyhuo.annotations.ResultEntity;

/**
 * Created by benny on 1/29/18.
 */
@GenerateBuilder(forResult = true,
        resultTypes = {@ResultEntity(name = "java", type = String.class), @ResultEntity(name = "kotlin", type=int.class)})
public class HelloInJavaActivity extends AppCompatActivity {

    @Required
    int num;

    @Optional
    boolean isJava;
}
