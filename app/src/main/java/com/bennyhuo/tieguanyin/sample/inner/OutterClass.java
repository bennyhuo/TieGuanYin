package com.bennyhuo.tieguanyin.sample.inner;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.GenerateMode;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;

public class OutterClass {
    @Builder(mode = GenerateMode.Both)
    public static class InnerClass extends Activity{

        @Required
        int a;

        @Optional
        boolean b;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Toast.makeText(this, this.getClass().getSimpleName() + "[a=" + a + ", b = " + b + "]", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
