package com.bennyhuo.tieguanyin.sample.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bennyhuo.tieguanyin.annotations.FragmentBuilder;
import com.bennyhuo.tieguanyin.annotations.Required;
import com.bennyhuo.tieguanyin.sample.R;

/**
 * Created by benny on 2/6/18.
 */
@FragmentBuilder
public class JavaFragment extends Fragment {

    @Required
    String text;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(text);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KotlinFragmentBuilderKt.showKotlinFragment(((ViewGroup)getView().getParent()), "From Java");
            }
        });
    }
}
