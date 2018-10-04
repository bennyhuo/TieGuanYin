package com.bennyhuo.tieguanyin.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.GenerateMode;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.annotations.ResultEntity;

@Builder(mode = GenerateMode.Both,
        resultTypes = {@ResultEntity(name = "name", type = String.class),
                @ResultEntity(name = "age", type = int.class),
                @ResultEntity(name = "title", type = String.class),
                @ResultEntity(name = "company", type = String.class)})
public class EditUserActivity extends Activity {

    @Required
    String name;

    @Required
    int age;

    @Required
    String title;

    @Required
    String company;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EditUserActivityBuilder.smartFinish(this,
                36, "Kotliner",
                "bennyhuo", "Kotlin Dev");
    }
}
