package com.bennyhuo.tieguanyin.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.GenerateMode;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;

@Builder(mode = GenerateMode.Both)
public class UserActivity extends Activity {

    @Required
    String name;

    @Required
    int age;

    @Optional
    String title;

    @Optional
    String company;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EditUserActivityBuilder.builder(30, "Kotlin", "bennyhuo", "Kotlin Developer")
                .start(this, new EditUserActivityBuilder.OnEditUserActivityResultListener() {
                    @Override
                    public void onResult(int age, String company, String name, String title) {

                    }
                });

    }

    public static void startUserActivity(Context context, String name, int age, String title, String company) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra("age", age);
        intent.putExtra("name", name);
        intent.putExtra("company", company);
        intent.putExtra("title", title);
        context.startActivity(intent);


    }

    public void inject(Bundle savedInstanceState) {
        this.age = savedInstanceState.getInt( "age");
        this.name = savedInstanceState.getString("name");
        this.company = savedInstanceState.getString("company" );
        this.title = savedInstanceState.getString("title");

        Intent intent = getIntent();
        this.age = intent.getIntExtra( "age", 0);
        this.name = intent.getStringExtra("name");
        this.company = intent.getStringExtra("company" );
        this.title = intent.getStringExtra("title");
    }

}
