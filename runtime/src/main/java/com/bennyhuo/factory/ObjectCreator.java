package com.bennyhuo.factory;

/**
 * Created by benny on 1/31/18.
 */

public interface ObjectCreator {
    <T> T create(Class<T> cls);
}
