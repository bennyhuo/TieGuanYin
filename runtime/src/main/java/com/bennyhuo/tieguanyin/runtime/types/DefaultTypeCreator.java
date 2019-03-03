package com.bennyhuo.tieguanyin.runtime.types;

import java.lang.reflect.Constructor;

public class DefaultTypeCreator implements TypeCreator {

    private final Class cls;

    public DefaultTypeCreator(Class cls) {
        this.cls = cls;
    }

    @Override
    public Object create() {
        try {
            Constructor constructor = cls.getConstructor();
            constructor.setAccessible(true);
            return cls.newInstance();
        } catch  (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Cannot create instance of " + cls);
    }
}
