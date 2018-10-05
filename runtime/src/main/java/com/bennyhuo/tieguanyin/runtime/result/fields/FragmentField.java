package com.bennyhuo.tieguanyin.runtime.result.fields;

import com.bennyhuo.tieguanyin.runtime.utils.Logger;

import java.lang.reflect.Field;

/**
 * Created by benny on 2/6/18.
 */

public class FragmentField extends ListenerField {
    public final String who;

    public FragmentField(Object object, Field field, String who) {
        super(object, field);
        Logger.debug("Who: " + who);
        this.who = who;
    }
}
