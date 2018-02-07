package com.bennyhuo.activitybuilder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.bennyhuo.activitybuilder.annotations.ActivityBuilder;
import com.bennyhuo.activitybuilder.annotations.GenerateMode;
import com.bennyhuo.activitybuilder.annotations.Optional;
import com.bennyhuo.activitybuilder.annotations.Required;
import com.bennyhuo.activitybuilder.annotations.ResultEntity;
import com.bennyhuo.activitybuilder.utils.ActivityUtils;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Created by benny on 1/29/18.
 */
@ActivityBuilder(forResult = true, mode = GenerateMode.JavaOnly,
        resultTypes = {@ResultEntity(name = "java", type = String.class), @ResultEntity(name = "kotlin", type=int.class)})
public class JavaActivity extends AppCompatActivity {

    @Required
    int num;

    @Optional
    boolean isJava;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(this.getClass().getSimpleName());

        Button button = findViewById(R.id.openJavaActivity);
        button.setText("Finish With java='I'm Java' & kotlin=2");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //JavaActivityBuilder.finishWithResult(JavaActivity.this, "I'm Java!!", 2);
            }
        });
        findViewById(R.id.openKotlinActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KotlinActivityBuilderKt.startKotlinActivity(JavaActivity.this, 1234, null, new Function2<String, Integer, Unit>() {
                    @Override
                    public Unit invoke(String s, Integer integer) {
                        ActivityUtils.toast(JavaActivity.this, "Result from KotlinActivity: s=" + s + "; integer=" + integer);
                        return Unit.INSTANCE;
                    }
                });
            }
        });

        findViewById(R.id.openGenerateBothActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateBothActivityBuilder.startWithOptionalNum(JavaActivity.this, 30, "bennyhuo", 1234);
            }
        });
    }
}
