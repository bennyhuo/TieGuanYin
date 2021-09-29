package com.bennyhuo.tieguanyin.sample.inherited;

import androidx.appcompat.app.AppCompatActivity;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.PendingTransition;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.annotations.ResultEntity;

/**
 * Created by benny on 2/9/18.
 */
@Builder(pendingTransition = @PendingTransition(enterAnim = 0, exitAnim = 0),
        resultTypes = @ResultEntity(name = "hello", type = String.class))
public abstract class AbsActivity extends AppCompatActivity {

    @Required
    public int fromSuper;

    @Optional
    public String fromSuperString;
}
