package com.bennyhuo.tieguanyin.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.annotations.ResultEntity;
import com.bennyhuo.tieguanyin.sample.inherited.AbsActivity;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Created by benny on 1/29/18.
 */
@Builder(resultTypes = {@ResultEntity(name = "javaMethod", type = String.class), @ResultEntity(name = "kotlin", type=int.class)})
public class JavaActivity extends AbsActivity {

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
                JavaActivityBuilder.finishWithResult(JavaActivity.this, "I'm Java!!", 2, "hello");
            }
        });
        findViewById(R.id.openKotlinActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KotlinActivityBuilderKt.startKotlinActivity(JavaActivity.this, 1234, null, new Function2<String[], int[], Unit>() {
                    @Override
                    public Unit invoke(String[] strings, int[] ints) {
                        return null;
                    }
                });
            }
        });

        findViewById(R.id.openGenerateBothActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GenerateBothActivityBuilder()
                        .num(1234)
                        .details("HelloWorld")
                        .subTitle("dfdf")
                        .startWithOptionals(JavaActivity.this, 30, "bennyhuo");
            }
        });
    }
}
