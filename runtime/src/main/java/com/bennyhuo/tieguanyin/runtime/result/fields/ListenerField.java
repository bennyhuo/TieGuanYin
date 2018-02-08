package com.bennyhuo.tieguanyin.runtime.result.fields;

import java.lang.reflect.Field;

/**
 * Created by benny on 2/6/18.
 */

public abstract class ListenerField {
    public final Field field;
    public final Object receiver;

    public ListenerField(Object receiver, Field field) {
        this.receiver = receiver;
        this.field = field;
        this.field.setAccessible(true);
    }

    public void update(Object object){
        try {
            field.set(this.receiver, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
