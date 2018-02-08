package com.bennyhuo.tieguanyin.runtime.result.fields;

import java.lang.reflect.Field;

/**
 * Created by benny on 2/6/18.
 */

public class FragmentField extends ListenerField {
    public final int id;

    public FragmentField(Object object, Field field, int id) {
        super(object, field);
        this.id = id;
    }
}
