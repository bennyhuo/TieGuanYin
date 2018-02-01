package com.bennyhuo.activitybuilder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        TextView textView = findViewById(R.id.clickMe);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent testIntent = new Intent();
//                testIntent.putExtra("kotlin", 1234567);
//                testIntent.putExtra("java", "I am Java!");
//                setResult(Activity.RESULT_OK, testIntent);
//
//                finish();

                HelloInJavaActivityBuilder.finishWithResult(HelloInJavaActivity.this, "JavaJava！！", 1231231);
            }
        });
    }
}
