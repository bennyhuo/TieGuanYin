package com.bennyhuo.activitybuilder.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bennyhuo.activitybuilder.R;
import com.bennyhuo.activitybuilder.annotations.FragmentBuilder;
import com.bennyhuo.activitybuilder.annotations.Required;

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
