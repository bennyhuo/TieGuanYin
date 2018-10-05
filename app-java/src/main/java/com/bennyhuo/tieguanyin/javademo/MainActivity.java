package com.bennyhuo.tieguanyin.javademo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bennyhuo.tieguanyin.annotations.Builder;
import com.bennyhuo.tieguanyin.annotations.Optional;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.javademo.fragments.JavaFragment;
import com.bennyhuo.tieguanyin.javademo.fragments.JavaFragmentBuilder;
import com.bennyhuo.tieguanyin.runtime.kotlin.fragment.Fragments;

@Builder
public class MainActivity extends AppCompatActivity {

    @Required
    String helloworld;

    @Optional
    int helloworld2;

    @Optional
    Integer helloworld3;

    @Optional
    double helloworld4;

    JavaFragment javaFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        javaFragment = JavaFragmentBuilder.builder("Hello World!!").replace(MainActivity.this, R.id.fragmentContainer);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(javaFragment.isHidden()){
                    Snackbar.make(view, "Show Java Fragment", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Fragments.show(javaFragment);
                } else {
                    Snackbar.make(view, "Hide Java Fragment", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Fragments.hide(javaFragment);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
