package com.bennyhuo.tieguanyin.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.annotations.ResultEntity;
import com.bennyhuo.tieguanyin.sample.fragments.JavaFragment;
import com.bennyhuo.tieguanyin.sample.fragments.JavaFragmentBuilder;
import com.bennyhuo.tieguanyin.sample.fragments.KotlinFragment;
import com.bennyhuo.tieguanyin.sample.fragments.KotlinFragmentBuilder;
import com.bennyhuo.tieguanyin.sample.inherited.AbsActivity;

import java.util.Arrays;

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

        final JavaFragment javaFragment = JavaFragmentBuilder.builder("HelloJava")
                .replace(this, R.id.fragmentContainer);

        final KotlinFragment kotlinFragment = KotlinFragmentBuilder.builder("HelloKotlin")
                .add(this, R.id.fragmentContainer);

        Button button = findViewById(R.id.openJavaActivity);
        button.setText("Finish With java='I'm Java' & kotlin=2");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JavaActivityBuilder.smartFinish(JavaActivity.this, "I'm Java!!", "hello", 2);
            }
        });
        findViewById(R.id.openKotlinActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KotlinActivityBuilderKt.startKotlinActivity(JavaActivity.this, 1234, null, new Function2<String[], int[], Unit>() {
                    @Override
                    public Unit invoke(String[] strings, int[] ints) {

                        Toast.makeText(JavaActivity.this, "strings=" + Arrays.toString(strings) + ", ints=" + Arrays.toString(ints), Toast.LENGTH_SHORT).show();

                        TextView textView = javaFragment.getView().findViewById(R.id.textView);
                        textView.setText(strings[0]);

                        TextView textView2 = kotlinFragment.getView().findViewById(R.id.textView);
                        textView2.setText(strings[1]);
                        return null;
                    }
                });
            }
        });

        findViewById(R.id.openGenerateBothActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateBothActivityBuilder.builder(30, "bennyhuo")
                        .num(1234)
                        .details("HelloWorld")
                        .subTitle("dfdf")
                        .start(JavaActivity.this);
            }
        });
    }

//    @Builder
//    public static class InnerActivity extends Activity{
//        @Required
//        int num;
//    }
}
