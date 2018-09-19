package com.bennyhuo.tieguanyin.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;

@Builder
public class UserActivity extends Activity {

    @Required
    String name;

    @Required
    int age;

    @Optional
    String title;

    @Optional
    String company;

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
    }

}
