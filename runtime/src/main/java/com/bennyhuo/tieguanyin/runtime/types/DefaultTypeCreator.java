package com.bennyhuo.tieguanyin.runtime.types;

import com.bennyhuo.tieguanyin.runtime.types.internal.UnsafeAllocator;

import java.lang.reflect.Constructor;

public class DefaultTypeCreator implements TypeCreator {

    public static final DefaultTypeCreator INSTANCE = new DefaultTypeCreator();

    private UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();

    private DefaultTypeCreator(){ }

    @Override
    public Object create(Class cls) {
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

        try {
            return unsafeAllocator.newInstance(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Cannot create instance of " + cls);
    }
}
