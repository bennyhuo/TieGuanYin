package com.bennyhuo.tieguanyin.runtime.result.fields;

import java.lang.reflect.Field;

/**
 * Created by benny on 2/6/18.
 */

public class ViewField extends ListenerField {
    public final int id;

    public ViewField(Object receiver, Field field, int id) {
        super(receiver, field);
        this.id = id;
    }
}
