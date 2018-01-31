package com.bennyhuo.factory.impl;

import com.bennyhuo.factory.ObjectCreator;

/**
 * Created by benny on 1/31/18.
 */

public class DefaultCreator implements ObjectCreator {

    @Override
    public <T> T create(Class<T> cls) {
        Object result = null;
        if (cls == boolean.class || cls == Boolean.class) {
            result = false;
        } else if (cls == int.class || cls == Integer.class) {
            result = 0;
        } else if (cls == short.class || cls == Short.class) {
            result = 0;
        } else if (cls == float.class || cls == Float.class) {
            result = 0f;
        } else if (cls == double.class || cls == Double.class) {
            result = 0.0;
        } else if (cls == char.class || cls == Character.class) {
            result = '\0';
        } else if(cls == String.class || cls == CharSequence.class){
            result = "";
        } else if(cls == Object[].class){
            result = new Object[0];
        }
        return (T) result;
    }
}
