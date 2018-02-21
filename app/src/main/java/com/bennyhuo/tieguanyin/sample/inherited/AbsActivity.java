package com.bennyhuo.tieguanyin.sample.inherited;

import android.support.v7.app.AppCompatActivity;

import com.bennyhuo.tieguanyin.annotations.ActivityBuilder;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.annotations.ResultEntity;

/**
 * Created by benny on 2/9/18.
 */
@ActivityBuilder(resultTypes = @ResultEntity(name = "hello", type = String.class))
public abstract class AbsActivity extends AppCompatActivity {

    @Required
    public int fromSuper;

    @Optional
    public String fromSuperString;
}
